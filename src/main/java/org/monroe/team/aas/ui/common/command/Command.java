package org.monroe.team.aas.ui.common.command;

/**
 * User: MisterJBee
 * Date: 6/30/13 Time: 7:02 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface Command <ReturnType, ArgumentType>  {
    public ReturnType callAndResult(ArgumentType argument);
}
