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
package net.sf.joost.grammar.tree;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.instruction.AbstractGroupBase;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Value;
import net.sf.joost.util.VariableNotFoundException;
import net.sf.joost.util.VariableUtils;

/**
 * Objects of VarTree represent variable reference ('$var') nodes in the syntax
 * tree of a pattern or an STXPath expression.
 *
 * @version $Revision: 1.6 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */
public final class VarTree extends AbstractTree
{
  /** The expanded name of the variable */
  private final String expName;

  private boolean scopeDetermined = false;
  private AbstractGroupBase groupScope = null;

  /*
   * Constructs a Tree object with a String value. If the type is a {@link
   * #NAME_TEST} then {@link #uri} and {@link #lName} will be initialized
   * appropriately according to the mapping given in {@link ParseContext#nsSet}.
   */
  public VarTree (final String value, final ParseContext context) throws SAXParseException
  {
    super (VAR, value);

    // value contains the qualified name
    final int colon = value.indexOf (":");
    if (colon != -1)
    {
      m_sURI = context.nsSet.get (value.substring (0, colon));
      if (m_sURI == null)
      {
        throw new SAXParseException ("Undeclared prefix '" + value.substring (0, colon) + "'", context.locator);
      }
      m_sLocalName = value.substring (colon + 1);
    }
    else
    {
      m_sURI = "";
      m_sLocalName = value;
    }
    expName = "{" + m_sURI + "}" + m_sLocalName;
  }

  @Override
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    if (!scopeDetermined)
    {
      try
      {
        groupScope = VariableUtils.findVariableScope (context, expName);
      }
      catch (final VariableNotFoundException e)
      {
        context.m_aErrorHandler.error ("Undeclared variable '" +
                                    m_aValue +
                                    "'",
                                    context.currentInstruction.m_sPublicID,
                                    context.currentInstruction.m_sSystemID,
                                    context.currentInstruction.lineNo,
                                    context.currentInstruction.colNo);
        // if the errorHandler decides to continue ...
        return Value.VAL_EMPTY;
      }
      scopeDetermined = true;
    }

    final Map <String, Value> vars = (groupScope == null) ? context.localVars
                                                          : context.groupVars.get (groupScope).peek ();

    final Value v1 = vars.get (expName);
    // create a copy if the result is a sequence
    return v1.next() == null ? v1 : v1.copy ();
  }

  @Override
  public boolean isConstant ()
  {
    return false;
  }

  @Override
  public AbstractTree deepCopy (final HashMap <Object, Object> copies)
  {
    final VarTree copy = (VarTree) super.deepCopy (copies);
    if (scopeDetermined && groupScope != null)
    {
      copy.groupScope = (AbstractGroupBase) groupScope.deepCopy (copies);
    }
    return copy;
  }
}
