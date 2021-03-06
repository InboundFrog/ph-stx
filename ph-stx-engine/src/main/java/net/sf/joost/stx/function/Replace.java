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
package net.sf.joost.stx.function;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;
import net.sf.joost.util.regex.IRegularExpression;
import net.sf.joost.util.regex.JRegularExpression;

/**
 * The <code>replace</code> function.<br>
 * Returns the string that is obtained by replacing each non-overlapping
 * substring of the first parameter that matches the given second parameter with
 * an occurrence of the third parameter string. The optional fourth parameter
 * influences the matching as in the {@link Matches} function.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-replace"> fn:replace in
 *      "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.2 $ $Date: 2007/06/13 20:29:07 $
 * @author Oliver Becker
 */
public final class Replace implements IInstance
{
  /** @return 3 **/
  public int getMinParCount ()
  {
    return 3;
  }

  /** @return 4 */
  public int getMaxParCount ()
  {
    return 4;
  }

  /** @return "replace" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "replace";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException,
                                                                                        EvalException
  {
    String input, pattern, replacement, flags;
    if (args.m_aLeft.m_aLeft.getType () == AbstractTree.LIST)
    {
      // four parameters
      input = args.m_aLeft.m_aLeft.m_aLeft.evaluate (context, top).getStringValue ();
      pattern = args.m_aLeft.m_aLeft.m_aRight.evaluate (context, top).getStringValue ();
      replacement = args.m_aLeft.m_aRight.evaluate (context, top).getStringValue ();
      flags = args.m_aRight.evaluate (context, top).getStringValue ();
    }
    else
    {
      // three parameters
      input = args.m_aLeft.m_aLeft.evaluate (context, top).getStringValue ();
      pattern = args.m_aLeft.m_aRight.evaluate (context, top).getStringValue ();
      replacement = args.m_aRight.evaluate (context, top).getStringValue ();
      flags = "";
    }
    final IRegularExpression re = new JRegularExpression (pattern, true, flags);
    if (re.matches (""))
      throw new EvalException ("The regular expression in replace() must " +
                               "not be one that matches a zero-length string");
    return new Value (re.replace (input, replacement));
  }
}
