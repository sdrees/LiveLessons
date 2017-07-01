package livelessons.utils;

import livelessons.platspec.PlatSpec;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class implements the Singleton pattern to handle
 * command-line option processing.
 */
public class Options {
    /** The singleton @a Options instance. */
    private static Options mUniqueInstance = null;

    /**
     * An enumeration of each different input source.
     */
    public enum InputSource {
    	DEFAULT,        // The default remote input source.
        DEFAULT_LOCAL,  // The default local input source.
        USER,           // Input from a user-defined source.
        FILE,           // Input from a delimited file.
        NETWORK,        // Input from a network call
        ERROR           // Returned if source is unrecognized.
    }

    /**
     * Keep track of the source of the inputs.
     */
    InputSource mInputSource = InputSource.DEFAULT_LOCAL;

    /**
     * Default image names to use for testing.
     */
    private final String[] mDefaultImageNames = new String[] {
          "ka.png,"
        + "uci.png,"
        + "dougs_small.jpg,"
        + "kitten.png,"
        + "schmidt_coursera.jpg,"
        + "dark_rider.jpg,"
        + "doug.jpg",
          "lil_doug.jpg,"
        + "ironbound.jpg,"
        + "wm.jpg,"
        + "robot.png,"
        + "ace_copy.jpg,"
        + "tao_copy.jpg,"
        + "doug_circle.png"
    };

    /**
     * Prefix for all the URLs.
     */
    private String mUrlPrefix = "http://www.dre.vanderbilt.edu/~schmidt/gifs/";

    /**
     * Pathname for the file containing URLs to download.
     */
    private String mPathname = "defaultUrls.txt";

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * Return pathname for the file containing the URLs to download.
     */
    public String getURLFilePathname() {
        return mPathname;
    }

    /**
     * Return the path for the directory where images are stored.
     */
    public String getDirectoryPath() {
        return new File("DownloadImages").getAbsolutePath();
    }

    /**
     * Takes a string input and returns the corresponding InputSource.
     */
    private InputSource getInputSource(String inputSource) {
        if (inputSource.equalsIgnoreCase("DEFAULT"))
            return InputSource.DEFAULT;
        else if (inputSource.equalsIgnoreCase("DEFAULT_LOCAL"))
            return InputSource.DEFAULT_LOCAL;
        else if (inputSource.equalsIgnoreCase("USER"))
            return InputSource.USER;
        else if (inputSource.equalsIgnoreCase("FILE"))
            return InputSource.FILE;
        else if (inputSource.equalsIgnoreCase("NETWORK"))
            return InputSource.NETWORK;
        else
            return InputSource.ERROR;
    }

    /**
     * Return an Iterator over one or more input URL Lists.
     */
    public Iterator<List<URL>> getUrlIterator() {
        List<List<URL>> urlLists =
            getUrlLists();
    	return urlLists != null && urlLists.size() > 0
            ? urlLists.iterator()
            : null;
    }

    /**
     * Gets the list of lists of URLs from which we want to download
     * images.
     */
    private List<List<URL>> getUrlLists() {
    	try {
            switch (mInputSource) {
            // If the user selects the defaults source, return the
            // default list of remote URL lists.
            case DEFAULT:
                return getDefaultUrlList(false);

            // If the user selects the default_local source, return the
            // default list of local URL lists.
            case DEFAULT_LOCAL:
                return getDefaultUrlList(true);

            // Take input from the Android UI.
            case USER:
            	/*
                for (int i = 0; i < numChildViews; ++i) {

                    // Convert the input string into a list of URLs
                    // and add it to the main list.
                    variableNumberOfInputURLs.add
                        (convertStringToUrls(child.getText().toString()));
                }
                */

                break;

            default:
                System.out.println("Invalid Source");
                return null;
            }
    	} catch (MalformedURLException e) {
            System.out.println("Invalid URL");
            return null;
    	}
    	return null;
    }

    /**
     * Returns the appropriate list of URLs, i.e., either pointing to
     * the local device or to a remote server.
     */
    private List<List<URL>> getDefaultUrlList(boolean local)
        throws MalformedURLException {
        return local
               ? getDefaultResourceUrlList()
               : getDefaultUrlList();
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    private List<List<URL>> getDefaultUrlList()
            throws MalformedURLException {
        return Stream
            // Convert the array of strings into a stream of strings.
            .of(mDefaultImageNames)

            // Map each string in the list into a list of URLs.
            .map(this::convertStringToUrls)

            // Create and return a list of a list of URLs.
            .collect(toList());
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    private List<List<URL>> getDefaultResourceUrlList()
            throws MalformedURLException {
        return PlatSpec.getDefaultResourceUrlList(null, mDefaultImageNames);
    }

    /**
     * Create a new URL list from a @a stringOfUrls that contains a
     * list of URLs separated by commas and add them to the URL list
     * that's returned.
     */
    private List<URL> convertStringToUrls(String stringOfNames) {
        // Create a Function that returns a new URL object when
        // applied and which converts checked URL exceptions into
        // runtime exceptions.
        Function<String, URL> urlFactory = 
            ExceptionUtils.rethrowFunction(URL::new);

        return Pattern
            // Create a regular expression for the "," separator.
            .compile(",")

            // Use regular expression to split stringOfNames into a
            // Stream<String>.
            .splitAsStream(stringOfNames)

            // Concatenate the url prefix with each name.
            .map(name -> mUrlPrefix + name)

            // Convert each string in the stream to a URL.
            .map(urlFactory::apply)

            // Create a list of URLs.
            .collect(toList());
    }

    /**
     * Returns whether debugging output is generated.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String argv[]) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
                    case "-d":
                        mDiagnosticsEnabled = argv[argc + 1].equals("true");
                        break;
                    case "-s":
                        mInputSource = getInputSource(argv[argc + 1]);
                        break;
                    default:
                        printUsage();
                        return false;
                }
            return true;
        } else
            return false;
    }

    /**
     * Print out usage and default values.
     */
    public void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-d [true|false]");
        System.out.println("-s [DEFAULT|DEFAULT_LOCAL|USER|FILE]");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
