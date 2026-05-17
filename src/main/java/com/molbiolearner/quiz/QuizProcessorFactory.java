package com.molbiolearner.quiz;

import com.molbiolearner.model.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizProcessorFactory {

    private final List<QuizProcessor> processors;

    public QuizProcessor getProcessor(QuizType type) {
        return processors.stream()
            .filter(processor -> processor.supports(type))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No quiz processor found for type: " + type));
    }
}
