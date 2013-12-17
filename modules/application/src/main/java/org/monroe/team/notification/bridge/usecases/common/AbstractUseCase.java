package org.monroe.team.notification.bridge.usecases.common;

public abstract class AbstractUseCase<Out, In> {

    private final UseCaseContext mCaseContext;

    public AbstractUseCase(UseCaseContext caseContext) {
        mCaseContext = caseContext;
    }

    final public <Type> Type strategy(Class<Type> aClass){
        return mCaseContext.getStrategy(aClass);
    }

    final public <Type> Type boundary(Class<Type> aClass){
        return mCaseContext.getBoundary(aClass);
    }

    public Out performForResult(In in){
        perform(in);
        return null;
    }

    protected void perform(In in) {}

}
