import com.jaemisseo.man.FileMan
import com.jaemisseo.man.VariableMan
import groovy.json.JsonSlurper
import install.task.TaskTestPort
import org.junit.Test

/**
 * Created by sujkim on 2017-02-26.
 */
class TempTest {


    @Test
    void "hello Test"(){
    }

    @Test
    void "test temp"(){
        List<String> funcs = ['d','s','j','k(','i']
        int funcStartIndex = funcs.findIndexOf{ it.indexOf('(') != -1 }
        String variable = funcs[0..funcStartIndex-1].join('.')
        List list = [variable] + funcs[funcStartIndex..funcs.size()-1]
        println list

    }

    @Test
    void "date format"(){
        println "${new Date().format('yyyyMMddHHmmssSSS')}"
    }


    @Test
    void "groovy range"(){
        (14..20).each{
            println it
        }

        ('f4'..'f9').each{
            println it
        }
    }

    @Test
    void "compress extract"(){
        String sourcePath = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\lib\\temp\\*'
        String destPath = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\test.zip'
        FileMan.zip(sourcePath, destPath, true)

        String sourcePath2 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\lib\\temp\\*'
        String destPath2 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\test.jar'
        FileMan.jar(sourcePath2, destPath2, true)

        String sourcePath3 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\lib\\temp\\*'
        String destPath3 = 'D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\test.tar.gz'
        FileMan.tar(sourcePath3, destPath3, true)

        FileMan.unzip(destPath)
        FileMan.unjar(destPath2)
        FileMan.untar(destPath3)
    }

    @Test
    void "file path range test"(){
        String path = "D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\*"
        List<String> entryList = FileMan.getFilePathList(path)
        println entryList.size()
        println path
        entryList.each{ println it }
        println ""

        String path2 = "D:\\dev_by_sj\\Dropbox\\workspacesForCompany\\installer\\build\\libs\\*te*.*ja*"
        List<String> entryList2 = FileMan.getFilePathList(path2)
        println entryList2.size()
        println path2
        entryList2.each{ println it }
        println ""

    }

    @Test
    void "VariableMan"(){

        //VariableMan 생성
        VariableMan varman = new VariableMan('EUC-KR', [
                USERNAME : "하이하이하이",
                lowerChar : "hi everybody",
                upperChar : "HI EVERYBODY",
                syntaxTest : 'HI ${}EVERYBODY${}',
                s : '하하하\\n하하하',
                s2 : 'ㅋㅋㅋ\nㅋㅋl',
                num: '010-9911-0321',
                'installer.level.1.file.path':'/foo/bar',

        ])
//        .setModeDebug(true)

        // Test - left() and right()
        assert varman.parse('${USERNAME(8).left()}asfd') == "하이하이asfd"
        assert varman.parse('${USERNAME()}asfd') == "하이하이하이asfd"
        assert varman.parse('${USERNAME}asfd')  == "하이하이하이asfd"
        assert varman.parse('${USERNAME(8)}asfd') == "하이하이asfd"
        assert varman.parse('${USERNAME(8).right()}asfd') == "하이하이asfd"
        assert varman.parse('${USERNAME(14).right()}asfd') == "하이하이하이  asfd"

        // Test - lower() and upper()
        assert varman.parse('${USERNAME(8).lower()}asfd') == "하이하이asfd"
        assert varman.parse('${lowerChar()}asfd') == "hi everybodyasfd"
        assert varman.parse('${lowerChar().upper()}asfd') == "HI EVERYBODYasfd"
        assert varman.parse('${upperChar(15).left(0).lower()}asfd') == "000hi everybodyasfd"
        assert varman.parse('${upperChar().lower()}asfd') == "hi everybodyasfd"

        // Test - ${}
        assert varman.parse('${}asdf') == '${}asdf'
        assert varman.parse('${}asdf${}') == '${}asdf${}'
        assert varman.parse('${}as ${ } ${  } ${  ddfd } ${ddfd} ${syntaxTest}  d   f${}') == '${}as ${ } ${  }   HI ${}EVERYBODY${}  d   f${}'
        assert varman.parse('${s()} ${s()}///${empty(50).left( )}///${}') == '하하하\\n하하하 하하하\\n하하하///                                                  ///${}'
        assert varman.parse('${s2()}///${empty(50).left( )}///${}') == 'ㅋㅋㅋ\nㅋㅋl///                                                  ///${}'

        // Test - numberOnly()
        assert varman.parse('[${num(15)}] / [${num().numberOnly()}] / [${num(15).numberOnly()}] / [${num(15).numberOnly().right()}]') == '[010-9911-0321  ] / [01099110321] / [01099110321    ] / [01099110321    ]'

        assert varman.parse('${installer.level.1.file.path}') == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path()}') == '/foo/bar'
        assert varman.parse('${installer.level.1.file.path(3)}') == '/fo'

        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(yyyy-MM-dd HH:mm:ssSSS)} ${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SSS)}${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(SS)}${}')
        // Test - date (내장된 변수)
        println varman.parse('${}as ${date(S)}${}')
        // Test - random (내장된변수)
        println varman.parse('[${date(yyyyMMddHHmmssSSS)}${random(5)}]')
    }

}
