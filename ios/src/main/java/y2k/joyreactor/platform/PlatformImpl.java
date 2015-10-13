package y2k.joyreactor.platform;

import org.robovm.apple.foundation.NSPathUtilities;
import org.robovm.apple.foundation.NSSearchPathDirectory;
import org.robovm.apple.foundation.NSSearchPathDomainMask;
import y2k.joyreactor.Navigation;
import y2k.joyreactor.Platform;

import java.io.File;
import java.util.List;

/**
 * Created by y2k on 29/09/15.
 */
public class PlatformImpl extends Platform {

    private static final File CURRENT_DIRECTORY = dogGetCurrentDirectory();

    private static File dogGetCurrentDirectory() {
        List<String> dirs = NSPathUtilities.getSearchPathForDirectoriesInDomains(
                NSSearchPathDirectory.DocumentDirectory,
                NSSearchPathDomainMask.UserDomainMask, true);
        return new File(dirs.get(0));
    }

    @Override
    public File getCurrentDirectory() {
        return CURRENT_DIRECTORY;
    }

    @Override
    public Navigation getNavigator() {
        return new StoryboardNavigation();
    }
}