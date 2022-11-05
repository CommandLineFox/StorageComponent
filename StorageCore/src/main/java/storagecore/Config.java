package storagecore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private double maxSizeLimit;
    private List<String> bannedExtensions;
    private Map<String, Integer> fileCountLimits;

    public Config() {
        maxSizeLimit = 16106127360.0;
        bannedExtensions = new ArrayList<>();
        fileCountLimits = new HashMap<>();
    }

    public Config(int maxSizeLimit, List<String> bannedExtensions, Map<String, Integer> fileCountLimits) {
        this.maxSizeLimit = maxSizeLimit;
        this.bannedExtensions = bannedExtensions;
        this.fileCountLimits = fileCountLimits;
    }

    public Config(double maxSizeLimit, List<String> bannedExtensions) {
        this.maxSizeLimit = maxSizeLimit;
        this.bannedExtensions = bannedExtensions;
        this.fileCountLimits = new HashMap<>();
    }

    public double getMaxSizeLimit() {
        return maxSizeLimit;
    }

    public void setMaxSizeLimit(double maxSizeLimit) {
        this.maxSizeLimit = maxSizeLimit;
    }

    public List<String> getBannedExtensions() {
        return bannedExtensions;
    }

    public void setBannedExtensions(List<String> bannedExtensions) {
        this.bannedExtensions = bannedExtensions;
    }

    public Map<String, Integer> getFileCountLimits() {
        return fileCountLimits;
    }

    public void setFileCountLimits(Map<String, Integer> fileCountLimits) {
        this.fileCountLimits = fileCountLimits;
    }
}
