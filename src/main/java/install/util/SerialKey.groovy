package install.util;

class SerialKey {



    static String generateAsStarCraftKey(){
        return generateSimple()
    }

    static String generate(int len){
        return generate(len, len, '-')
    }

    static String generate(int len, int cut, String hipen){
        return new SerialKey().generate_auto_mix_key(len, cut, hipen)
    }




    /****************************************************************************************************
     *
     * CDKey Generator (StarCraft Method)
     *
     ****************************************************************************************************/
    private static String generateSimple() {
        String rndKey = "";
        for(int i=0; i<12; i++) {
            rndKey += (int)(Math.random() * 10);
            if(i==3 || i==8) rndKey += "-";
        }
        return rndKey + getChecksum(rndKey);
    }

    private static long getChecksum(String generatedKey) {
        long checksum = 3L;
        for(int i=1; i<=generatedKey.length(); i++) {
            if(i != 5 && i != 11) {
                checksum += (Long.parseLong(generatedKey.substring(i-1,i))^(2*checksum));
            }
        }
        return checksum % 10;
    }

    /** 스타크레프트 식 **/
    void generator() {
        int eax=3, edx=0, edi, lastnumber;
        List<Integer> serial = [];

        for (int i = 0; i < 12; i++)
            serial[i] = Math.random() % 10;

        for (int i = 0; i < 12; i++){
            edi = eax ^ (eax * 2);
            eax += edi;
        }
        lastnumber  = eax % 10;

        //Print
        serial.each{ println it }
        println lastnumber
    }



    /****************************************************************************************************
     *
     * CDKey Generator (Great Method)
     *
     ****************************************************************************************************/
    //지정된 자릿수의 랜덤한 숫자를 반환합니다.
    // 최대 10까지 가능합니다.
    // 4 이면 1000 에서 9999 사이의 랜덤 숫자
    int get_rand_number(int len) {
        len = Math.abs((int)len);
        if (len < 1)
            len = 1;
        else if (len > 10)
            len = 10;
        return rand( Math.pow(10, len - 1), (Math.pow(10, len) - 1) );
    }

    //넘어온 세자리수를 36진수로 변환해서 반환합니다.
    // preg_match_callback 을 통해서만 사용됩니다.
    String get_simple_36(int m){
        String str = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        int div = Math.floor(m / 36);
        int rest = m % 36;
        return "${str[div]}${str[rest]}";
    }

    //지정된 자리수에 존재하는 소수 전체를 배열로 반환합니다.
    // max len = 5
    List<Integer> get_simple_prime_number(int len){
        len = Math.abs((int)len);
        if (len < 1)
            len = 1;
        else if (len > 5)
            len = 5;

        List<Integer> $prime_1 = [1, 2, 3, 5, 7];
        if (len == 1)
            return $prime_1;

        int $start = Math.pow(10, (len - 1)) + 1;//101
        int $end = Math.pow(10, len) - 1;//999
        List<Integer> prime = $prime_1;
        prime.remove(0);//1제거
        prime.remove(1);//2제거
        return makePrimeNumberArray(prime, $start, $end);
    }

    List<Integer> makePrimeNumberArray(List<Integer> prime, int start, int end){
        List<Integer> array = []
        boolean isContinue2 = false
        for (int i=11; i<=end; i+=2){//10보다 큰 소수에는 짝수가 없다.
            int max = Math.floor(Math.sqrt(i));

            for (int j : prime) {
                if (j > max)
                    break;
                if ((i % j) == 0){
                    isContinue2 = true
                    break;
                }
            }
            if (isContinue2){
                isContinue2 = false
                continue
            }
            prime << i;
            if (i >= start)
                array << i;
        }
        return array
    }

    //지정된 자릿수의 숫자로된 시리얼을 반환합니다.
    // - 를 포함하고 싶지 않을때는 $cut 이 $len 보다 크거나 같으면 됩니다.
    // max len = 36
    String get_serial(int len, int cut, String hipen){
        len = Math.abs((int)len);
        if (len < 1) len = 16; else if (len > 36) len = 36;
        cut = Math.abs((int)cut);
        if (cut < 1) cut = 4; else if (cut > len) cut = len;

        String[] microtimeArray = explode(' ', microtime())
        String usec = str_replace('0.', '', microtimeArray[0])
        String sec = microtimeArray[1]
        String base_number = "${sec}${usec}"

        base_number = "${base_number}${get_rand_number(10)}${get_rand_number(8)}";//36자리 유니크한 숫자 문자열
        List<Integer> prime = get_simple_prime_number(5);//5자리 소수 배열
        shuffle(prime)

        String serial = bcmul(substr(base_number, 0, len), prime[0]);
        int sub = len - strlen(serial);

        if (sub > 0)
            serial = "${serial}${get_rand_number(sub)}";
        else if (sub < 0)
            serial = serial.substring(0, len);

        return toHipenedKey(serial, cut, hipen)
    }

    //지정된 자릿수의 숫자와 영문으로된 시리얼을 반환합니다.
    // - 를 포함하고 싶지 않을때는 $cut 이 $len 보다 크거나 같으면 됩니다.
    // max len = 24
    String generate_auto_mix_key(int len, int cut, String hipen){
        len = Math.abs((int)len);
        if (len < 1)
            len = 16;
        else if (len > 24)
            len = 24;

        cut = Math.abs((int)cut);
        if (cut < 1)
            cut = 4;
        else if (cut > len)
            cut = len;

        int len2 = (int)(len * 3 / 2);
        if (len2 % 2 == 1)
            len2 += 1;

        String serial = get_serial(len2, len2, hipen);
        serial = substr(to36by3digit(serial), 0, len);
        return toHipenedKey(serial, cut, hipen)
    }




    String to36by3digit(String key){
        String resultString = ''
        String groupString = ''
        key.eachWithIndex{ String c, int i ->
            c = (c == '.') ? '0' : c
            groupString += c
            if ( groupString.length() == 3 || i >= (key.length() -1) ){
                resultString += get_simple_36(Integer.parseInt(groupString))
                groupString = ''
            }
        }
        return resultString
    }


    String toHipenedKey(String key, int cut, String hipen){
        String resultString = ''
        String groupString = ''
        key.eachWithIndex{ String c, int i ->
            c = (c == '.') ? '0' : c
            resultString += c
            groupString += c
            if (groupString.length() == cut && i < (key.length() -1)){
                resultString += hipen
                groupString = ''
            }
        }
        return resultString
    }

    /**************************************************
     *
     *  PHP to JAVA
     *
     *   - abs(INT)
     *     - Math.abs(INT)
     *   -
     **************************************************/
    public static int rand(double n1, double n2) {
        return (int) (Math.random() * (n2 - n1 + 1)) + n1;
    }

    public static String[] explode(String token, String string){
        String regex = "[" + token + "]"
        return string.split(regex);
    }

    public static String str_replace(String target, String replacement, String original){
        return original.replace(target, replacement);
    }

    public static String substr(String original, int startIndex, int endIndex){
        endIndex = (endIndex > original.length()) ? original.length() : endIndex
        return original.substring(startIndex, endIndex);
    }

    public static int strlen(String string){
        return string.length();
    }

    public static List shuffle(List list){
        Collections.shuffle( list, new Random(System.nanoTime()) );
        return list;
    }

    public static String bcmul(Object a, Object b){
        return bcmul(a, b, 0);
    }

    public static String bcmul(Object a, Object b, int scale){
        double value = Double.parseDouble(String.valueOf(a)) * Double.parseDouble(String.valueOf(b));
        if (scale > 0){
            value = Math.floor(value * scale) * (Math.pow(0.1, scale));
        }
        return new BigDecimal(value).toString();
    }

    public static String microtime(){
        long now = new Date().getTime() / 1000;
        double nt = System.nanoTime();
        double micro = (nt  / 1000000000.0) - Math.floor(nt  / 1000000000.0);
        return micro.toString() + " " + (now).toString();
    }

}