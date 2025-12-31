package com.google.logbot.service;

import dev.langchain4j.service.SystemMessage;

public interface LogAssistant {

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
