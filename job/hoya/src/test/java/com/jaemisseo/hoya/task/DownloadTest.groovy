package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.application.HoyaCliApplication
import jaemisseo.man.configuration.context.Command
import org.junit.Ignore
import org.junit.Test

class DownloadTest {

    @Test
    @Ignore
    void test(){
        new HoyaCliApplication().run(new Command(
                "-download",
                "https://github.com/avaj-java/installer-maker/releases/download/0.8.3.1/installer-maker-cli-0.8.3.1.zip",
                "./build/download-test/installer-maker-cli-0.8.3.1.zip"
        ));


    }


    @Test
    @Ignore
    void test2(){
        new HoyaCliApplication().run(new Command(
                "-download",
                "http://192.168.0.119:8090/download/attachments/22623796/IRUDA_%EC%84%A4%EC%B9%98%EA%B0%80%EC%9D%B4%EB%93%9C_v3.0.docx?api=v2",
                "./build/download-test/something.docx",
                "-auth-raw=sujkim:godqhrgo100%"
        ));

    }

    @Test
    @Ignore
    void test3(){
        new HoyaCliApplication().run(new Command(
                "-path",
                "D:/***.txt"
        ));
    }

}
