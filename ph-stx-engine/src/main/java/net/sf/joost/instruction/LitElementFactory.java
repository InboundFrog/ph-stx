/*
 * $Id: LitElementFactory.java,v 2.16 2008/10/04 17:13:14 obecker Exp $
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is: this file
 *
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.instruction;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

import net.sf.joost.CSTX;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for literal result elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.16 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class LitElementFactory
{
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String uri,
                                      final String lName,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context,
                                      final Hashtable <String, String> newNamespaces) throws SAXParseException
  {
    if (parent == null)
    {
      if (lName.equals ("transform"))
        throw new SAXParseException ("File is not an STX transformation sheet, need namespace '" +
                                     CSTX.STX_NS +
                                     "' for the 'transform' element",
                                     context.locator);
      throw new SAXParseException ("File is not an STX transformation sheet, found " + qName, context.locator);
    }

    if (parent instanceof TransformFactory.Instance)
      throw new SAXParseException ("Literal result element '" +
                                   qName +
                                   "' may occur only within templates",
                                   context.locator);

    final AbstractTree [] avtList = new AbstractTree [attrs.getLength ()];
    for (int i = 0; i < avtList.length; i++)
      avtList[i] = AbstractFactoryBase.parseAVT (attrs.getValue (i), context);

    return new Instance (uri, lName, qName, attrs, avtList, parent, context, newNamespaces);
  }

  /** Represents a literal result element. */

  public static final class Instance extends AbstractNodeBase
  {
    private String uri;
    private final String lName;
    private final AttributesImpl attrs;
    private AbstractTree [] avtList;
    // the namespaces that possibly need a declaration in the output
    private Hashtable <String, String> namespaces;
    private final Map <String, String> namespaceAliases;

    protected Instance (final String uri,
                        final String lName,
                        final String qName,
                        final Attributes attrs,
                        final AbstractTree [] avtList,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final Hashtable <String, String> newNamespaces)
    {
      super (qName, parent, context, true);
      this.uri = uri;
      this.lName = lName;
      this.attrs = new AttributesImpl (attrs);
      this.avtList = avtList;

      // store namespaces
      if (newNamespaces.size () > 0)
      {
        namespaces = newNamespaces; // no copy required
        for (final Enumeration <String> keys = namespaces.keys (); keys.hasMoreElements ();)
        {
          final String key = keys.nextElement ();
          // remove the namespaces from exclude-result-prefixes
          if (context.transformNode.m_aExcludedNamespaces.contains (namespaces.get (key)))
            namespaces.remove (key);
          // remove the namespace that belongs to this qName
          if (qName.startsWith (key) &&
              uri.equals (namespaces.get (key)) &&
              ((key.equals ("") && qName.indexOf (':') == -1) || (qName.indexOf (':') == key.length ())))
            namespaces.remove (key);

        }
        if (namespaces.size () == 0) // no namespace left
          namespaces = null;
      }
      // else: namespaces = null

      this.namespaceAliases = context.transformNode.m_aNamespaceAliases;
    }

    /**
     * Determine constant attribute values and apply all declared namespaces
     * aliases (<code>stx:namespace-alias</code>)
     */
    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      if (pass == 0)
      {
        // evalute constant attribute values
        boolean allConstant = true;
        for (int i = 0; i < avtList.length; i++)
        {
          if (avtList[i].isConstant ())
          {
            attrs.setValue (i, avtList[i].evaluate (null, -1).getString ());
            avtList[i] = null;
          }
          else
            allConstant = false;
        }
        if (allConstant) // no need to iterate over the array
          avtList = new AbstractTree [0];

        // For applying the declared namespaces we have to wait until the
        // whole STX sheet has been parsed
        return true;
      }

      if (namespaceAliases.size () == 0)
                                        // no aliases declared
                                        return false;

      // Change namespace URI of this element
      String toNS = namespaceAliases.get (uri);
      if (toNS != null)
      {
        uri = toNS;
        int colon;
        if (toNS == "" && (colon = m_sQName.indexOf (':')) != -1)
        {
          // target null namespace must be used unprefixed
          m_sQName = m_sQName.substring (colon + 1);
        }
        else
          if (NamespaceSupport.XMLNS.equals (toNS))
          {
            // target XML namespace must use the xml prefix
            m_sQName = "xml:" + m_sQName.substring (m_sQName.indexOf (':') + 1);
          }
      }

      // Change namespace URI of the attributes
      final int aLen = attrs.getLength ();
      for (int i = 0; i < aLen; i++)
      {
        final String aURI = attrs.getURI (i);
        // process only prefixed attributes
        if (aURI != "")
        {
          toNS = namespaceAliases.get (aURI);
          if (toNS != null)
          {
            attrs.setURI (i, toNS);
            if (toNS == "")
            {
              // target null namespace must be used unprefixed
              final String aQName = attrs.getQName (i);
              attrs.setQName (i, aQName.substring (aQName.indexOf (':') + 1));
              // indexOf mustn't return -1 since aURI != ""
            }
            else
              if (NamespaceSupport.XMLNS.equals (toNS))
              {
                // target XML namespace must use the xml prefix
                final String aQName = attrs.getQName (i);
                attrs.setQName (i, "xml" + aQName.substring (aQName.indexOf (':')));
              }
          }
        }
      }

      if (namespaces != null)
      {
        // Change namespace URIs of in-scope namespaces
        for (final Enumeration <String> keys = namespaces.keys (); keys.hasMoreElements ();)
        {
          final String key = keys.nextElement ();
          final String value = namespaces.get (key);
          final String alias = namespaceAliases.get (value);
          if (alias == "" || NamespaceSupport.XMLNS.equals (alias))
            namespaces.remove (key);
          else
            if (alias != null)
              namespaces.put (key, alias);
        }
      }

      return false;
    }

    /**
     * Emits the start tag of this literal element to the emitter
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      // attrs is not cloned at the moment (see onDeepCopy(..)), so a
      // synchronization is necessary
      synchronized (attrs)
      {
        for (int i = 0; i < avtList.length; i++)
          if (avtList[i] != null)
            attrs.setValue (i, avtList[i].evaluate (context, this).getString ());
        context.m_aEmitter.startElement (uri, lName, m_sQName, attrs, namespaces, this);
      }
      return CSTX.PR_CONTINUE;
    }

    /**
     * Emits the end tag of this literal element to the emitter
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.m_aEmitter.endElement (uri, lName, m_sQName, m_aNodeEnd);
      return super.processEnd (context);
    }

    /**
     * @return a copy of the namespaces that have to be checked for a possible
     *         redeclaration
     */
    public Hashtable <String, String> getNamespaces ()
    {
      return namespaces != null ? new Hashtable<> (namespaces) : new Hashtable<> ();
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      theCopy.avtList = new AbstractTree [avtList.length];
      for (int i = 0; i < avtList.length; i++)
        if (avtList[i] != null)
          theCopy.avtList[i] = avtList[i].deepCopy (copies);
    }

    //
    // for debugging
    //
    @Override
    public String toString ()
    {
      return "LitElement <" + m_sQName + ">";
    }
  }
}
