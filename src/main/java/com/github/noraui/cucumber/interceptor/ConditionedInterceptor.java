/**
 * NoraUi is licensed under the licence GNU AFFERO GENERAL PUBLIC LICENSE
 * 
 * @author Nicolas HALLOUIN
 * @author Stéphane GRILLON
 */
package com.github.noraui.cucumber.interceptor;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.noraui.cucumber.annotation.Conditioned;
import com.github.noraui.gherkin.GherkinStepCondition;
import com.github.noraui.utils.Context;
import com.github.noraui.utils.Messages;

public class ConditionedInterceptor implements MethodInterceptor {

    /**
     * Specific logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ConditionedInterceptor.class);

    private static final String SKIPPED_DUE_TO_CONDITIONS = "SKIPPED_DUE_TO_CONDITIONS";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //
        Method m = invocation.getMethod();

        if (m.isAnnotationPresent(Conditioned.class)) {
            Object[] arg = invocation.getArguments();
            if (arg.length > 0 && arg[arg.length - 1] instanceof List && !((List) arg[arg.length - 1]).isEmpty() && ((List) arg[arg.length - 1]).get(0) instanceof GherkinStepCondition) {
                List<GherkinStepCondition> conditions = (List) arg[arg.length - 1];
                displayMessageAtTheBeginningOfMethod(m.getName(), conditions);
                if (!checkConditions(conditions)) {
                    Context.getCurrentScenario().write(Messages.getMessage(SKIPPED_DUE_TO_CONDITIONS));
                    return Void.TYPE;
                }
            }
        }

        logger.debug("NORAUI ConditionedInterceptor invoke method {}", invocation.getMethod());
        return invocation.proceed();
    }

    /**
     * Display a message at the beginning of method.
     *
     * @param methodName
     *            is the name of method for logs
     * @param conditions
     *            list of 'expected' values condition and 'actual' values ({@link com.github.noraui.gherkin.GherkinStepCondition}).
     */
    private void displayMessageAtTheBeginningOfMethod(String methodName, List<GherkinStepCondition> conditions) {
        logger.debug("{} with {} contition(s)", methodName, conditions.size());
        displayConditionsAtTheBeginningOfMethod(conditions);
    }

    /**
     * Display a message at the beginning of method.
     *
     * @param methodName
     *            is the name of method for logs
     * @param field
     *            is the value of parameter
     * @param conditions
     *            list of 'expected' values condition and 'actual' values ({@link com.github.noraui.gherkin.GherkinStepCondition}).
     */
    protected void displayMessageAtTheBeginningOfMethod(String methodName, String field, List<GherkinStepCondition> conditions) {
        logger.debug("{}: {} with {} contition(s)", methodName, field, conditions.size());
        displayConditionsAtTheBeginningOfMethod(conditions);
    }

    /**
     * Display all conditions at the beginning of method.
     *
     * @param conditions
     *            list of 'expected' values condition and 'actual' values ({@link com.github.noraui.gherkin.GherkinStepCondition}).
     */
    private void displayConditionsAtTheBeginningOfMethod(List<GherkinStepCondition> conditions) {
        int i = 0;
        for (GherkinStepCondition gherkinCondition : conditions) {
            i++;
            logger.debug("  expected contition N°{}={}", i, gherkinCondition.getExpected());
            logger.debug("  actual   contition N°{}={}", i, gherkinCondition.getActual());
        }
    }

    /**
     * Check all conditions.
     *
     * @param conditions
     *            list of 'expected' values condition and 'actual' values ({@link com.github.noraui.gherkin.GherkinStepCondition}).
     * @return a boolean
     */
    public boolean checkConditions(List<GherkinStepCondition> conditions) {
        for (GherkinStepCondition gherkinCondition : conditions) {
            logger.debug("checkConditions {} in context is ", gherkinCondition.getActual(), Context.getValue(gherkinCondition.getActual()));
            if (!gherkinCondition.checkCondition()) {
                return false;
            }
        }
        return true;
    }

}
