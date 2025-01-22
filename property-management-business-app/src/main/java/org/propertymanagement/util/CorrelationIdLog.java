package org.propertymanagement.util;

import java.util.Map;
import java.util.concurrent.Callable;

public interface CorrelationIdLog {
    String START = "S";
    String PREFIX_SEPARATOR = "-";

    /**
     * Execution of action either with an internally autogenerated correlationId provided
     * @param correlationIdPrefix Correlation prefix. Format: <Prefix> + PREFIX_SEPARATOR + <VALUE>
     * @param action Action to be executed
     */
    void execWithAutogeneratedCorrelationId(String correlationIdPrefix, Runnable action);

    /**
     * Execution of action either with an internally autogenerated correlationId provided
     * @param correlationIdPrefix Correlation prefix. Format: <Prefix> + PREFIX_SEPARATOR + <VALUE>
     * @param action Action to be executed
     * @return Result of execution
     * @param <V> Return type
     */
    <V> V execWithAutogeneratedCorrelationId(String correlationIdPrefix, Callable<V> action);
    /**
     * Execution of action either with an provided correlationId
     * @param correlationId Correlation identifier. Format: <Prefix> + PREFIX_SEPARATOR + <VALUE>
     * @param action Action to be executed
     */
    void execWithProvidedCorrelationId(String correlationId, Runnable action);

    /**
     * Execution of action either with an provided correlationId
     * @param correlationId Correlation identifier. Format: <Prefix> + PREFIX_SEPARATOR + <VALUE>
     * @param action Action to be executed
     * @return Result of execution
     * @param <V> Return type
     */
    <V> V execWithProvidedCorrelationId(String correlationId, Callable<V> action);

    /**
     * Return correlationId associated to the action in execution
     * @return Correlation identifier
     */
    String getCorrelationId();

    Map<String, String> getCopyOfContextMap();

    void setContextMap(Map<String, String> contextMap);
}
