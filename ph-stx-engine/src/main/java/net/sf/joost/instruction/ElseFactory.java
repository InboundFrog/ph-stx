/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016-2017 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.instruction;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>else</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.4 $ $Date: 2008/01/09 11:16:06 $
 * @author Oliver Becker
 */

public class ElseFactory extends AbstractFactoryBase
{
  /** @return <code>"else"</code> */
  @Override
  public String getName ()
  {
    return "else";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    checkAttributes (qName, attrs, null, context);

    if (!(parent.m_aLastChild.getNode () instanceof IfFactory.Instance))
      throw new SAXParseException ("Found '" + qName + "' without stx:if", context.locator);

    return new Instance (qName, parent, context);
  }

  /**
   * Represents an instance of the <code>else</code> element.
   */
  public static final class Instance extends AbstractNodeBase
  {
    public Instance (final String qName, final AbstractNodeBase parent, final ParseContext context)
    {
      super (qName, parent, context, true);
    }

    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      if (pass == 0) // following sibling not available yet
        return true;

      mayDropEnd ();
      return false;
    }

    // no special process() and processEnd() needed
  }
}
