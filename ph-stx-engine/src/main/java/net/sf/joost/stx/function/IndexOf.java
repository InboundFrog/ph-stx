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

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.grammar.tree.EqTree;
import net.sf.joost.grammar.tree.ValueTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>index-of</code> function.<br>
 * Returns a sequence of integer numbers, each of which is the index of a member
 * of the specified sequence that is equal to the item that is the value of the
 * second argument.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-index-of"> fn:index-of in
 *      "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class IndexOf implements IInstance
{
  /** @return 2 */
  public int getMinParCount ()
  {
    return 2;
  }

  /** @return 2 */
  public int getMaxParCount ()
  {
    return 2;
  }

  /** @return "index-of" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "index-of";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException, EvalException
  {
    Value seq = args.m_aLeft.evaluate (context, top);
    final Value item = args.m_aRight.evaluate (context, top);

    if (seq.type() == Value.EMPTY)
      return seq;

    final AbstractTree tSeq = new ValueTree (seq);

    // We shouldn't be mutating sequence values, so break out now
    if(item.next() != null) {
      throw new IllegalArgumentException("item parameter should not be a sequence");
    }

    final AbstractTree tItem = new ValueTree (item);
    // use the implemented = semantics
    final AbstractTree equals = new EqTree (tSeq, tItem);

    Value next, last = null, result = Value.VAL_EMPTY;
    long index = 1;

    while (seq != null)
    {
      next = seq.next();
      seq.next(null); // compare items, not sequences
      if (equals.evaluate (context, top).getBooleanValue ())
      {
        if (last == null)
          last = result = new Value (index);
        else {
          last.next(new Value(index));
          last = last.next();
        }
      }
      tSeq.m_aValue = seq = next;
      index++;
    }

    return result;
  }
}
