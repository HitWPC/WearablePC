package cn.hitftcl.wearablepc.Message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.litepal.crud.DataSupport;

import cn.hitftcl.wearablepc.Model.Expression;
import cn.hitftcl.wearablepc.R;

public class ExpressionEditActivity extends AppCompatActivity {

    private EditText mEditText;

    private Button mButtonEdit;

    private Button mButtonDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secret_activity_expression_edit);
        //设置ToolBar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_expression_edit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final int expressionId = intent.getIntExtra("expression_id", 0);
        final int position = intent.getIntExtra("position", -1);
        final Expression expression = DataSupport.find(Expression.class, expressionId);

        mEditText = (EditText)findViewById(R.id.id_edit_content);
        mEditText.setText(expression.getContent());
        mButtonEdit = (Button)findViewById(R.id.id_btn_edit);
        mButtonDelete = (Button)findViewById(R.id.id_btn_delete);

        mButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEditText.getText().toString().trim();
                expression.setContent(content);
                expression.update(expressionId);

                Intent intent = new Intent();
                intent.putExtra("result", "edit");
                intent.putExtra("position", position);
                intent.putExtra("content", content);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSupport.delete(Expression.class, expressionId);

                Intent intent = new Intent();
                intent.putExtra("result", "delete");
                intent.putExtra("position", position);
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
