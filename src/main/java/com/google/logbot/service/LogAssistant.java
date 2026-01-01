package com.google.logbot.service;

import dev.langchain4j.service.SystemMessage;

/**
 * AI Service Interface supported by LangChain4j.
 * <p>
 * Defines the contract for interacting with the LLM.
 * The system message instructs the AI on how to behave and which tools to use.
 * </p>
 */
public interface LogAssistant {

    /**
     * Sends a user message to the AI and receives a response.
     * The AI may call tools or retrieve RAG content before answering.
     *
     * @param userMessage Natural language query from the user.
     * @return The AI's textual response.
     */
    @SystemMessage("""
                You are a helpful Log Analysis Assistant.
                You have access to tools that can summarize errors from a database and retrieve detailed logs.

                If the user asks for a summary or count of errors, USE THE `getErrorSummary` tool.
                If the user asks for details about a specific error code, USE THE `getErrorDetails` tool.
                For general questions, use your knowledge base (RAG) which contains the log entries.

                Always answer in a polite and professional manner.
            """)
    String chat(String userMessage);
}
