package com.jaemisseo.man

import com.jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-18.
 */
class QuestionMan {

    public static final int QUESTION_TYPE_FREE = 1
    public static final int QUESTION_TYPE_YN = 2
    public static final int QUESTION_TYPE_CHOICE = 3

    public static final String Y = 'Y'
    public static final String N = 'N'

    QuestionSetup gOpt = new QuestionSetup()
    QuestionSetup nowOpt
    String yourAnswer

    /**
     * SET
     */
    QuestionMan set(QuestionSetup opt){
        gOpt.merge(opt)
        nowOpt = gOpt
        return this
    }

    /**
     * QUESTION
     */
    String question(){
        return question(gOpt, null)
    }

    String question(QuestionSetup lOpt){
        return question(lOpt, null)
    }

    String question(Closure validClosure){
        return question(gOpt, validClosure)
    }

    String question(int questionType){
        return question(gOpt, questionType)
    }

    String question(QuestionSetup lOpt, int questionType){
        return question(lOpt, getValidAnswerClosure(questionType))
    }

    String question(QuestionSetup lOpt, Closure validAnswerClosure){
        nowOpt = gOpt.clone().merge(lOpt)
        String yourAnswer
        String question = nowOpt.question
        String recommandAnswer = nowOpt.recommandAnswer ?: ''
        Map descriptionMap = nowOpt.descriptionMap

        //Ask Question
        int repeatLimit = 5
        int repeatCount = 0
        boolean isOk = false

        while (!isOk){
            if (repeatCount++ > repeatLimit)
                throw new Exception('So Many Not Good Answer. Please Correct Answer :) ')

            //Print Question
            println "${question} [${recommandAnswer}]? "

            //Print Selection
            if (descriptionMap)
                descriptionMap.sort{ a,b -> a.key <=> b.key }.each{ println "  ${it.key}) ${it.value}" }

            //Wait Your Input
            print "> "
            yourAnswer = new Scanner(System.in).nextLine()

            //If You Just Enter, Input Recommand Answer
            if (!yourAnswer)
                yourAnswer = recommandAnswer

            //Valid Answer ?
            isOk = (validAnswerClosure && validAnswerClosure(yourAnswer, nowOpt)) || (!validAnswerClosure)

            //Check Answer
            println "=> ${yourAnswer}\n"
            if (!isOk)
                println "!! Not Good Answer. Please Answer Angain"
        }

        this.yourAnswer = yourAnswer
        return yourAnswer
    }


    String getValue(){
        return getValue(yourAnswer)
    }

    String getValue(String answer){
        Map valMap = nowOpt.valueMap
        return (valMap && valMap[answer]) ? valMap[answer] : answer
    }

    Closure getValidAnswerClosure(int questionType){
        Closure resultClosure
        switch (questionType){
            case QUESTION_TYPE_FREE:
                resultClosure = null
                break
            case QUESTION_TYPE_YN:
                resultClosure = { String answer, QuestionSetup opt ->
                    answer = answer ? answer.toUpperCase() : ''
                    return (answer.equals(Y) || answer.equals(N))
                }
                break
            case QUESTION_TYPE_CHOICE:
                resultClosure = { String answer, QuestionSetup opt ->
                    return answer && ( (opt.valueMap && opt.valueMap.containsKey(answer)) || (opt.descriptionMap && opt.descriptionMap.containsKey(answer)) )
                }
                break
            default:
                break
        }
        return resultClosure
    }


}
