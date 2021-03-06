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
package jflex;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all rules of the specification for later access in RegExp -> NFA
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class RegExps
{

  /** the spec line in which a regexp is used */
  List <Integer> lines;

  /** the lexical states in wich the regexp is used */
  List <List <Integer>> states;

  /** the regexp */
  List <RegExp> regExps;

  /** the action of a regexp */
  List <Action> actions;

  /** flag if it is a BOL regexp */
  List <Boolean> BOL;

  /** the lookahead expression */
  List <RegExp> look;

  /** the forward DFA entry point of the lookahead expression */
  List <Integer> look_entry;

  /**
   * Count of many general lookahead expressions there are. Need
   * 2*gen_look_count additional DFA entry points.
   */
  int gen_look_count;

  public RegExps ()
  {
    states = new ArrayList<> ();
    regExps = new ArrayList<> ();
    actions = new ArrayList<> ();
    BOL = new ArrayList<> ();
    look = new ArrayList<> ();
    lines = new ArrayList<> ();
    look_entry = new ArrayList<> ();
  }

  public int insert (final int line,
                     final List <Integer> stateList,
                     final RegExp regExp,
                     final Action action,
                     final Boolean isBOL,
                     final RegExp lookAhead)
  {
    if (Options.DEBUG)
    {
      Out.debug ("Inserting regular expression with statelist :" + Out.NL + stateList); //$NON-NLS-1$
      Out.debug ("and action code :" + Out.NL + action.content + Out.NL); //$NON-NLS-1$
      Out.debug ("expression :" + Out.NL + regExp); //$NON-NLS-1$
    }

    states.add (stateList);
    regExps.add (regExp);
    actions.add (action);
    BOL.add (isBOL);
    look.add (lookAhead);
    lines.add (line);
    look_entry.add (null);

    return states.size () - 1;
  }

  public int insert (final List <Integer> stateList, final Action action)
  {

    if (Options.DEBUG)
    {
      Out.debug ("Inserting eofrule with statelist :" + Out.NL + stateList); //$NON-NLS-1$
      Out.debug ("and action code :" + Out.NL + action.content + Out.NL); //$NON-NLS-1$
    }

    states.add (stateList);
    regExps.add (null);
    actions.add (action);
    BOL.add (null);
    look.add (null);
    lines.add (null);
    look_entry.add (null);

    return states.size () - 1;
  }

  public void addStates (final int regNum, final List <Integer> newStates)
  {
    states.get (regNum).addAll (newStates);
  }

  public int getNum ()
  {
    return states.size ();
  }

  public boolean isBOL (final int num)
  {
    return BOL.get (num);
  }

  public RegExp getLookAhead (final int num)
  {
    return look.get (num);
  }

  public boolean isEOF (final int num)
  {
    return BOL.get (num) == null;
  }

  public List <Integer> getStates (final int num)
  {
    return states.get (num);
  }

  public RegExp getRegExp (final int num)
  {
    return regExps.get (num);
  }

  public int getLine (final int num)
  {
    return lines.get (num);
  }

  public int getLookEntry (final int num)
  {
    return look_entry.get (num);
  }

  public void checkActions ()
  {
    if (actions.get (actions.size () - 1) == null)
    {
      Out.error (ErrorMessages.NO_LAST_ACTION);
      throw new GeneratorException ();
    }
  }

  public Action getAction (int num)
  {
    while (num < actions.size () && actions.get (num) == null)
      num++;

    return actions.get (num);
  }

  public int NFASize (final Macros macros)
  {
    int size = 0;
    for (final RegExp r : regExps)
      if (r != null)
        size += r.size (macros);

    for (final RegExp r : look)
      if (r != null)
        size += r.size (macros);

    return size;
  }

  public void checkLookAheads ()
  {
    for (int i = 0; i < regExps.size (); i++)
      lookAheadCase (i);
  }

  /**
   * Determine which case of lookahead expression regExpNum points to (if any).
   * Set case data in corresponding action. Increment count of general lookahead
   * expressions for entry points of the two additional DFAs. Register DFA entry
   * point in RegExps Needs to be run before adding any regexps/rules to be able
   * to reserve the correct amount of space of lookahead DFA entry points.
   *
   * @param regExpNum
   *        the number of the regexp in RegExps.
   */
  private void lookAheadCase (final int regExpNum)
  {
    if (getLookAhead (regExpNum) != null)
    {
      final RegExp r1 = getRegExp (regExpNum);
      final RegExp r2 = getLookAhead (regExpNum);

      final Action a = getAction (regExpNum);

      final int len1 = SemCheck.length (r1);
      final int len2 = SemCheck.length (r2);

      if (len1 >= 0)
      {
        a.setLookAction (Action.FIXED_BASE, len1);
      }
      else
        if (len2 >= 0)
        {
          a.setLookAction (Action.FIXED_LOOK, len2);
        }
        else
          if (SemCheck.isFiniteChoice (r2))
          {
            a.setLookAction (Action.FINITE_CHOICE, 0);
          }
          else
          {
            a.setLookAction (Action.GENERAL_LOOK, 0);
            look_entry.set (regExpNum, gen_look_count);
            gen_look_count++;
          }
    }
  }

}
