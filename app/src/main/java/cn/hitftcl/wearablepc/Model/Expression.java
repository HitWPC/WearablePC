package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

public class Expression extends DataSupport {
    private int id;
    private String content;

    public Expression() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
