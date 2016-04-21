package main.java.osn.info;

import java.util.ArrayList;
import java.util.List;

public class XClassificationInfo {
    public String intent;

    public List<String> entities;

    public XClassificationInfo()
    {
        entities = new ArrayList<String>();
    }
}
