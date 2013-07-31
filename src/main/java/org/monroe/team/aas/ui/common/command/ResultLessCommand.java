package org.monroe.team.aas.ui.common.command;

/**
 * User: MisterJBee
 * Date: 6/30/13 Time: 7:02 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class ResultLessCommand<ArgumentType> implements Command<Void, ArgumentType> {
    @Override
    public Void callAndResult(ArgumentType argument) {
        call(argument);
        return null;
    }

    protected abstract void call(ArgumentType argument);

}
