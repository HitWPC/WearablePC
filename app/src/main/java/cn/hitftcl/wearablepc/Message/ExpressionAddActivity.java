package cn.hitftcl.wearablepc.Message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.hitftcl.wearablepc.Model.Expression;
import cn.hitftcl.wearablepc.R;

public class ExpressionAddActivity extends AppCompatActivity {


    private EditText mEditText;

    private Button mButtonAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secret_activity_expression_add);
        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_expression_add);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditText = (EditText)findViewById(R.id.id_add_content);
        mButtonAdd = (Button)findViewById(R.id.id_btn_add);

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEditText.getText().toString().trim();
                Expression expression = new Expression();
                expression.setContent(content);
                expression.save();

                Intent intent = new Intent();
                intent.putExtra("id", expression.getId());
                intent.putExtra("content", content);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * toolbar返回按钮响应事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
