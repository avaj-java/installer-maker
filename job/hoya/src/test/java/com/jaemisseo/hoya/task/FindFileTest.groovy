package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.application.HoyaCliApplication
import org.junit.Ignore
import org.junit.Test
import jaemisseo.man.configuration.context.Command

import java.util.regex.Matcher
import java.util.regex.Pattern

class FindFileTest {

    @Test
    @Ignore
    void matcher_capture(){
        boolean matchesByRegex = "D:/big_install/apache-tomcat-8.5.45/webapps/docs/appdev/sample/src/mypackage/Hello.java".matches("(?<DIR>.*)/(?<NAME>.*)[.]java")
        assert matchesByRegex

        Matcher matcher = Pattern.compile("(?<DIR>.*)/(?<NAME>.*)\\.java").matcher("D:/big_install/apache-tomcat-8.5.45/webapps/docs/appdev/sample/src/mypackage/Hello.java")
        assert matcher.matches()
        assert matcher.group("DIR").equals("D:/big_install/apache-tomcat-8.5.45/webapps/docs/appdev/sample/src/mypackage")
        assert matcher.group("NAME").equals("Hello")
    }

    @Test
    @Ignore
    void findAll_from_root(){
        new HoyaCliApplication().run(new Command(
//                "-questionfindfile",
//                '-desc=ㄱㄱ',
                "-findfile",
                '-find.root.path=D:\\',
                '-find.file.name=D:/**/*.java',
//                '-find.if="{\"external.properties\":true, \"../../WEB-INF\":true}"',
//                '-find.result.edit.relpath: ../../',
                '-find.result.edit.refactoring.pattern=#{dir}/#{name}.java',
                '-find.result.edit.refactoring.result=#{name}',
//                '-find.result.edit.refactoring.pattern=#{*}/gradle-#{level}-#{alias}.properties',
//                '-find.result.edit.refactoring.result=#{alias}',
                '-mode.recursive=true'
        ));
    }

    @Test
    @Ignore
    void findAll_one_level_files_from_some_dir(){
        new HoyaCliApplication().run(new Command(
                "-questionfindfile",
                '-desc=ㄱㄱ',
                '-find.root.path=D:\\임시소스\\tutorials\\xml\\src\\test\\java\\com\\baeldung\\xml',
                '-find.file.name=*.java',
//                '-find.if="{\"external.properties\":true, \"../../WEB-INF\":true}"',
//                '-find.result.edit.relpath: ../../',
                '-find.result.edit.refactoring.pattern=#{dir}/#{name}.java',
                '-find.result.edit.refactoring.result=#{name}',
//                '-find.result.edit.refactoring.pattern=#{*}/gradle-#{level}-#{alias}.properties',
//                '-find.result.edit.refactoring.result=#{alias}',
                '-mode.recursive=true'
        ));
    }

    @Test
    void findAll_recursively_from_some_dir(){
        new HoyaCliApplication().run(new Command(
                "-questionfindfile",
                '-desc=ㄱㄱㄱㄱ',
                '-find.root.path=D:\\임시소스\\tutorials\\xml\\src\\test\\java\\com\\baeldung\\xml',
                '-find.file.name=**/*.java',
//                '-find.if="{\"external.properties\":true, \"../../WEB-INF\":true}"',
//                '-find.result.edit.relpath: ../../',
                '-find.result.edit.refactoring.pattern=#{dir}/#{name}.java',
                '-find.result.edit.refactoring.result=#{name}',
//                '-find.result.edit.refactoring.pattern=#{something}/gradle-#{level}-#{alias}.properties',
//                '-find.result.edit.refactoring.result=#{alias}',
                '-mode.recursive=true'
        ));

    }

    @Test
    @Ignore
    void findAll_from_user_dir(){
        new HoyaCliApplication().run(new Command(
                "-questionfindfile",
                "D:/***.txt"
        ));

    }

}
