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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.stx.function;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>avg</code> function.<br>
 * Returns the average value of the sequence.
 *
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class Avg implements IInstance
{
  /** @return 1 */
  public int getMinParCount ()
  {
    return 1;
  }

  /** @return 1 */
  public int getMaxParCount ()
  {
    return 1;
  }

  /** @return "avg" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "avg";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException, EvalException
  {
    Value v = args.evaluate (context, top);
    if (v.type == Value.EMPTY) // empty sequence
      return v;
    double avg = 0;
    int count = 0;
    while (v != null)
    {
      final Value next = v.next;
      avg += v.getNumberValue ();
      count++;
      v = next;
    }
    return new Value (avg / count);
  }
}
