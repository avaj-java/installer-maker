package temp.util

/**
 * Created by sujkim on 2017-06-27.
 */
class StreamGobbler implements Runnable {

    private InputStream inputStream;

    public StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        List<String> lineList = new BufferedReader(new InputStreamReader(inputStream)).readLines()
        lineList.each{
            println it
        }
    }
}
