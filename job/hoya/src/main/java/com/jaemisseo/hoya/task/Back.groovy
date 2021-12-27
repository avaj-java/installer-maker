package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.Task

@Task
class Back extends TaskHelper{

    @Override
    Integer run(){
        return STATUS_BACK
    }

}
