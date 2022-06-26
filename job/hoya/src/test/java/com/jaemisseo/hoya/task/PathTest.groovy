package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.application.HoyaCliApplication
import org.junit.Test
import jaemisseo.man.configuration.context.Command

class PathTest {

    @Test
    void test(){
        new HoyaCliApplication().run(new Command(
                "-path",
                "D:/*.txt"
        ));
    }


    @Test
    void test2(){
        new HoyaCliApplication().run(new Command(
                "-path",
                "D:/temp/**/*"
        ));

    }

    @Test
    void test3(){
        new HoyaCliApplication().run(new Command(
                "-path",
                "D:/***.txt"
        ));
    }

}
