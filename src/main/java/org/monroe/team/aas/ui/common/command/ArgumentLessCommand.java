package org.monroe.team.aas.ui.common.command;

/**
 * User: MisterJBee
 * Date: 6/30/13 Time: 7:02 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class ArgumentLessCommand<ResultType> implements Command<ResultType, Void> {
    @Override
    public ResultType callAndResult(Void argument) {
        return call();
    }

    protected abstract ResultType call();
}
