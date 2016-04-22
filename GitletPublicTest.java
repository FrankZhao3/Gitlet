import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.sun.media.jai.opimage.AddCollectionCRIF;

/**
 * Class that provides JUnit tests for Gitlet, as well as a couple of utility
 * methods.
 * 
 * @author Joseph Moghadam
 * 
 *         Some code adapted from StackOverflow:
 * 
 *         http://stackoverflow.com/questions
 *         /779519/delete-files-recursively-in-java
 * 
 *         http://stackoverflow.com/questions/326390/how-to-create-a-java-string
 *         -from-the-contents-of-a-file
 * 
 *         http://stackoverflow.com/questions/1119385/junit-test-for-system-out-
 *         println
 * 
 */
public class GitletPublicTest {
    private static final String GITLET_DIR = ".gitlet/";
    private static final String TESTING_DIR = "test_files/";

    /* matches either unix/mac or windows line separators */
    private static final String LINE_SEPARATOR = "\r\n|[\r\n]";

    /**
     * Deletes existing gitlet system, resets the folder that stores files used
     * in testing.
     * 
     * This method runs before every @Test method. This is important to enforce
     * that all tests are independent and do not interact with one another.
     */
    @Before
    public void setUp() {
        File f = new File(GITLET_DIR);
        if (f.exists()) {
            recursiveDelete(f);
        }
        f = new File(TESTING_DIR);
        if (f.exists()) {
            recursiveDelete(f);
        }
        f.mkdirs();
    }

    /**
     * Tests that init creates a .gitlet directory. Does NOT test that init
     * creates an initial commit, which is the other functionality of init.
     */
    @Test
    public void testBasicInitialize() {
        gitlet("init");
        File f = new File(GITLET_DIR);
        assertTrue(f.exists());
    }

    /**
     * Tests that checking out a file name will restore the version of the file
     * from the previous commit. Involves init, add, commit, and checkout.
     */
    @Test
    public void testBasicCheckout() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String wugText = "This is a wug.";
        createFile(wugFileName, wugText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        writeFile(wugFileName, "This is not a wug.");
        gitlet("checkout", wugFileName);
        assertEquals(wugText, getText(wugFileName));
    }

    // checkout ID
    @Test
    public void testCheckoutID() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);

        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("add", catFileName);
        gitlet("commit", "added cat");
        writeFile(wugFileName, "This is not a wug.");
        writeFile(catFileName, "This is not a cat.");
        gitlet("checkout", "1", wugFileName);
        assertEquals(wugText, getText(wugFileName));
        gitlet("checkout", "2", catFileName);
        assertEquals(catText, getText(catFileName));
    }

    @Test
    public void testCheckoutBranch() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        writeFile(wugFileName, "This is not a wug.");
        gitlet("branch", "branch1");
        System.out.println(gitlet("checkout", "branch1"));
        gitlet("status");
        // assertEquals(wugText, getText(wugFileName));
    }

    @Test
    public void testBaiscBranchLog() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("branch", "branch1");
        gitlet("checkout", "branch1");
        gitlet("add", catFileName);
        gitlet("commit", "added cat");
        gitlet("checkout", "master");
        gitlet("add", dogFileName);
        gitlet("commit", "added dog");
        gitlet("checkout", "branch1");
        gitlet("log");
    }

    @Test
    public void testBaiscMerge1() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("branch", "branch1");
        gitlet("checkout", "branch1");
        gitlet("add", catFileName);
        gitlet("commit", "added cat");
        gitlet("checkout", "master");
        gitlet("add", dogFileName);
        gitlet("commit", "added dog");
        writeFile(catFileName, "This is not a cat.");
        gitlet("merge", "branch1");
        gitlet("merge", "branch1");
        assertEquals(catText, getText(catFileName));
    }

    @Test
    public void testBaiscMerge2() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("branch", "branch1");
        gitlet("add", dogFileName);
        gitlet("commit", "added dog");
        writeFile(dogFileName, "This is not a dog.");
        gitlet("merge", "branch1");
        assertEquals("This is not a dog.", getText(dogFileName));
    }

    // it has conflict cat.txt
    @Test
    public void testBaiscMerge3() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("branch", "branch1");
        gitlet("checkout", "branch1");
        gitlet("add", catFileName);
        gitlet("commit", "added cat");
        gitlet("checkout", "master");
        gitlet("add", catFileName);
        gitlet("commit", "added cat again");
        writeFile(catFileName, "This is not a cat.");
        gitlet("merge", "branch1");
        assertEquals("This is not a cat.", getText(catFileName));
    }

    @Test
    public void testBasicCommit() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("add", catFileName);
        gitlet("add", dogFileName);
        gitlet("commit", "added cat");
        writeFile(wugFileName, "This is not a wug.");
        gitlet("checkout", wugFileName);
        writeFile(dogFileName, "This is not a dog.");
        gitlet("checkout", dogFileName);
        assertEquals("This is a wug.", getText(wugFileName));
        assertEquals("This is a dog.", getText(dogFileName));

    }

    @Test
    public void testRemoveFile() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        System.out.println("********");
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("rm", wugFileName);
        gitlet("commit", "rm");
        System.out.println(gitlet("log"));
        System.out.println(gitlet("checkout", wugFileName));
        System.out.println("********");
    }

    /**
     * Tests that log prints out commit messages in the right order. Involves
     * init, add, commit, and log.
     */
    @Test
    public void testBaiscMerge4() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", dogFileName);
        gitlet("add", wugFileName);
        gitlet("commit", "added dog");
        gitlet("branch", "branch1");
        gitlet("checkout", "branch1");

        writeFile(dogFileName, "This is not a dog.");
        gitlet("add", dogFileName);
        gitlet("commit", "added dog modified");

        gitlet("branch", "branch2");
        gitlet("checkout", "branch2");

        gitlet("rm", dogFileName);
        gitlet("commit", "rm dog");

        gitlet("branch", "branch3");
        gitlet("checkout", "branch3");

        writeFile(dogFileName, "This is a dog.");
        gitlet("add", dogFileName);
        gitlet("add", catFileName);
        gitlet("commit", "save dog back and cat added");

        gitlet("checkout", "branch1");
        gitlet("add", catFileName);
        gitlet("commit", "cat added again");
        writeFile(wugFileName, "This is not a wug.");
        gitlet("add", wugFileName);
        gitlet("commit", "change wug");
        gitlet("checkout", "master");

        // System.out.println(getText(dogFileName));
        gitlet("add", dogFileName);
        gitlet("add", catFileName);
        gitlet("commit", "end");
        //
        gitlet("merge", "branch1");
        File afile3 = new File(catFileName + ".conflicted");
        assertEquals(true, afile3.exists());
        assertEquals("This is a cat.", getText(catFileName));

        gitlet("merge", "branch3");
        File file2 = new File(catFileName + ".conflicted");
        assertEquals(true, file2.exists());
        assertEquals(dogText, getText(dogFileName));

        writeFile(wugFileName, "This is a wug.");
        gitlet("merge", "branch1");
        File afile = new File(catFileName + ".conflicted");
        assertEquals(true, afile.exists());
        assertEquals("This is a cat.", getText(catFileName));
        assertEquals("This is not a wug.", getText(wugFileName));
        //
        writeFile(wugFileName, "This is adsadadaadsada wug.");
        // System.out.println(gitlet("global-log"));
        gitlet("reset", "6");
        System.out.println(gitlet("log"));
        System.out.println(gitlet("checkout", "6", "wug.txt"));
        assertEquals("This is not a wug.", getText(wugFileName));
    }

    @Test
    public void testCheckout1() {
        gitlet("init");
        String commitMessage1 = "initial commit";
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String wugText1 = "This is a wug.";
        String catText1 = "cat";
        createFile(wugFileName, wugText1);
        createFile(catFileName, catText1);
        gitlet("add", wugFileName);
        gitlet("add", catFileName);
        String commitMessage2 = "added wug and cat";
        gitlet("commit", commitMessage2);
        String catText2 = "cat2";
        gitlet("add", catFileName);
        writeFile(catFileName, catText2);
        gitlet("add", catFileName);
        String commitMessage3 = "change cat";
        gitlet("commit", commitMessage3);
        String logContent = gitlet("log");
        assertArrayEquals(new String[] { commitMessage3, commitMessage2,
                commitMessage1 }, extractCommitMessages(logContent));
        gitlet("checkout", "1", catFileName);
        assertEquals(catText1, getText(catFileName));
    }

    @Test
    public void testBasicBranchLog() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("branch", "branch1");
        gitlet("checkout", "branch1");
        gitlet("add", catFileName);
        gitlet("commit", "added cat");
        gitlet("checkout", "master");
        gitlet("add", dogFileName);
        gitlet("commit", "added dog");
        gitlet("checkout", "branch1");
        // System.out.println(gitlet("log"));
    }

    @Test
    public void testBasicLog() {
        gitlet("init");
        String commitMessage1 = "initial commit";

        String wugFileName = TESTING_DIR + "wug.txt";
        String wugText = "This is a wug.";
        createFile(wugFileName, wugText);
        gitlet("add", wugFileName);
        String commitMessage2 = "added wug";
        gitlet("commit", commitMessage2);
        gitlet("log");
        String logContent = gitlet("log");
        assertArrayEquals(new String[] { commitMessage2, commitMessage1 },
                extractCommitMessages(logContent));
    }

    @Test
    public void testBaiscRebase1() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        gitlet("init");
        gitlet("add", wugFileName);
        gitlet("commit", "added wug");
        gitlet("branch", "branch1");
        gitlet("checkout", "branch1");
        System.out.println("----- wug   " + getText(wugFileName));
        assertEquals(wugText, getText(wugFileName));
        gitlet("add", catFileName);
        gitlet("commit", "added cat");
        System.out.println("----- cat   " + getText(catFileName));
        assertEquals(catText, getText(catFileName));
        gitlet("checkout", "master");
        assertEquals(wugText, getText(wugFileName));
        System.out.println("----- wug   " + getText(wugFileName));
        gitlet("add", dogFileName);
        gitlet("commit", "added dog");
        System.out.println("----- dog   " + getText(dogFileName));
        writeFile(catFileName, "This is not a cat.");
        System.out.println(gitlet("rebase", "branch1"));
        System.out.println("----- dog   " + getText(dogFileName));
        System.out.println(gitlet("log"));
        assertEquals(catText, getText(catFileName));
        assertEquals(dogText, getText(dogFileName));

    }

    // @Test
    // public void testRebase4() {
    // String wugFileName = TESTING_DIR + "wug.txt";
    // String catFileName = TESTING_DIR + "cat.txt";
    // String dogFileName = TESTING_DIR + "dog.txt";
    // String barFileName = TESTING_DIR + "bar.txt";
    // String fooFileName = TESTING_DIR + "foo.txt";
    // String wugText = "This is a wug.";
    // String catText = "This is a cat.";
    // String dogText = "This is a dog.";
    // String barText = "This is a car.";
    // String fooText = "This is a foo.";
    // createFile(wugFileName, wugText);
    // createFile(catFileName, catText);
    // createFile(dogFileName, dogText);
    // createFile(barFileName, barText);
    // createFile(fooFileName, fooText);
    // gitlet("init");
    // gitlet("add", dogFileName);
    // gitlet("add", catFileName);
    // gitlet("commit", "add dog and cat commit 1");
    // gitlet("branch", "test1");
    // String dogText1 = "This is a not dog.";
    // writeFile(dogFileName, dogText1);
    // gitlet("add", dogFileName);
    // gitlet("add", barFileName);
    // gitlet("commit", "add dog and bar commit 2");
    // gitlet("rm", catFileName);
    // gitlet("commit", "rm cat commit 3");
    // gitlet("checkout", "test1");
    // String catText1 = "This is not a cat.";
    // writeFile(catFileName, catText1);
    // gitlet("add", catFileName);
    // gitlet("add", fooFileName);
    // gitlet("commit", "add at and foo commit 4");
    // gitlet("rm", dogFileName);
    // gitlet("commit", "rm dog commit 5");
    // gitlet("checkout", "master");
    // gitlet("rebase", "test1");
    // System.out.println("============  rebase 4 \n" + gitlet("log"));
    // assertEquals(catText1, getText(catFileName));
    // assertEquals(fooText, getText(fooFileName));
    // assertEquals(dogText1, getText(dogFileName));
    // assertEquals(barText, getText(barFileName));
    // System.out.println("============  rebase 4 \n" + gitlet("log"));
    // }
    // @Test
    public void testMerge5() {
        String wugFileName = TESTING_DIR + "wug.txt";
        String catFileName = TESTING_DIR + "cat.txt";
        String dogFileName = TESTING_DIR + "dog.txt";
        String barFileName = TESTING_DIR + "bar.txt";
        String fooFileName = TESTING_DIR + "foo.txt";
        String wugText = "This is a wug.";
        String catText = "This is a cat.";
        String dogText = "This is a dog.";
        String barText = "This is a bar.";
        String fooText = "This is a foo.";
        createFile(wugFileName, wugText);
        createFile(catFileName, catText);
        createFile(dogFileName, dogText);
        createFile(barFileName, barText);
        createFile(fooFileName, fooText);
        gitlet("init");
        gitlet("add", dogFileName);
        gitlet("add", catFileName);
        gitlet("commit", "dog and cat");
        gitlet("branch", "branch1");
        gitlet("add", barFileName);
        System.out.println(gitlet("commit", "bar"));
        writeFile(barFileName, "This is not a dog");
        System.out.println(gitlet("checkout", barFileName));
        assertEquals("This is a bar.", getText(barFileName));
        gitlet("checkout", "master");
        gitlet("add", wugFileName);
        gitlet("commit", "wug");
        gitlet("add", barFileName);
        gitlet("commit", "added bar");
        System.out.println(gitlet("merge", "branch1"));
        File afile = new File(barFileName + ".conflicted");
        // System.out.println(gitlet("log"));
        assertEquals(true, afile.exists());
        // assertEquals("This is a cat.", getText(catFileName));
        // assertEquals("This is not a wug.", getText(wugFileName));
    }

    /**
     * Convenience method for calling Gitlet's main. Anything that is printed
     * out during this call to main will NOT actually be printed out, but will
     * instead be returned as a string from this method.
     * 
     * Prepares a 'yes' answer on System.in so as to automatically pass through
     * dangerous commands.
     * 
     * The '...' syntax allows you to pass in an arbitrary number of String
     * arguments, which are packaged into a String[].
     */
    private static String gitlet(String... args) {
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream printingResults = new ByteArrayOutputStream();
        try {
            /*
             * Below we change System.out, so that when you call
             * System.out.println(), it won't print to the screen, but will
             * instead be added to the printingResults object.
             */
            System.setOut(new PrintStream(printingResults));

            /*
             * Prepares the answer "yes" on System.In, to pretend as if a user
             * will type "yes". You won't be able to take user input during this
             * time.
             */
            String answer = "yes";
            InputStream is = new ByteArrayInputStream(answer.getBytes());
            System.setIn(is);

            /* Calls the main method using the input arguments. */
            Gitlet.main(args);

        } finally {
            /*
             * Restores System.out and System.in (So you can print normally and
             * take user input normally again).
             */
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        return printingResults.toString();
    }

    /**
     * Returns the text from a standard text file (won't work with special
     * characters).
     */
    private static String getText(String fileName) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Creates a new file with the given fileName and gives it the text
     * fileText.
     */
    private static void createFile(String fileName, String fileText) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeFile(fileName, fileText);
    }

    /**
     * Replaces all text in the existing file with the given text.
     */
    private static void writeFile(String fileName, String fileText) {
        FileWriter fw = null;
        try {
            File f = new File(fileName);
            fw = new FileWriter(f, false);
            fw.write(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes the file and all files inside it, if it is a directory.
     */
    private static void recursiveDelete(File d) {
        if (d.isDirectory()) {
            for (File f : d.listFiles()) {
                recursiveDelete(f);
            }
        }
        d.delete();
    }

    /**
     * Returns an array of commit messages associated with what log has printed
     * out.
     */
    private static String[] extractCommitMessages(String logOutput) {
        String[] logChunks = logOutput.split("====");
        int numMessages = logChunks.length - 1;
        String[] messages = new String[numMessages];
        for (int i = 0; i < numMessages; i++) {
            // System.out.println(logChunks[i + 1]);
            String[] logLines = logChunks[i + 1].split(LINE_SEPARATOR);
            messages[i] = logLines[3];
        }
        return messages;
    }
}
