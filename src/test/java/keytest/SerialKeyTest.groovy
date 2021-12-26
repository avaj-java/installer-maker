package keytest

import com.jaemisseo.hoya.util.SerialKey
import org.junit.Test

class SerialKeyTest {

    @Test
    void generateSerialKey(){
        int repeatCount = 3
        List<String> keyList = []
        keyList += (1..repeatCount).collect{ SerialKey.generate(24, 6, '-'); }
        keyList += (1..repeatCount).collect{ SerialKey.generate(20, 5, '-'); }
        keyList += (1..repeatCount).collect{ SerialKey.generate(20, 4, '-'); }
        keyList += (1..repeatCount).collect{ SerialKey.generate(15, 3, '-'); }
        keyList += (1..repeatCount).collect{ SerialKey.generate(10, 2, '-'); }
        keyList += (1..repeatCount).collect{ SerialKey.generate(8, 4, '-'); }
        keyList += (1..repeatCount).collect{ SerialKey.generate(8, 2, '-'); }
        keyList += (1..repeatCount).collect{ SerialKey.generateAsStarCraftKey(); }
        keyList.each{
            println it
        }
    }
}
