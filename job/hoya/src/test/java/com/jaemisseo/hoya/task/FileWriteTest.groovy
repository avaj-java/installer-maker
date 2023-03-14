package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.application.HoyaCliApplication
import jaemisseo.man.configuration.context.Command
import org.junit.Ignore
import org.junit.Test

class FileWriteTest {

    @Test
//    @Ignore
    void test(){
        new HoyaCliApplication().run(new Command(
                "-filewrite",
                "-file=test_some.properties",
                "-contents.properties.a=1",
                "-contents.properties.ba=\${product.name}",
                "-contents.properties.ca3",
                "-contents.properties.de=\${exec(git describe --tags)}",
//                "-contents.properties={\n" +
//                        "            \"name\": \"\${product.name}\",\n" +
//                        "            \"version\": \"\${product.version}\",\n" +
//                        "            \"tag\": \"\${exec(git describe --tags --abbrev=0)}\",\n" +
//                        "            \"commit\": \"\${exec(git rev-parse --short HEAD)}\",\n" +
//                        "            \"branch\": \"\${exec(git rev-parse --abbrev-ref HEAD)}\"\n" +
//                        "        }"
//                "-contents.test={\"some\":\"\${exec(git describe --tags)}\"}",
        ));
    }

}
