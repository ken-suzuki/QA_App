package jp.techacademy.ken.suzuki.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    // お気に入りボタンの変数を定義
    private Button mLikeButton;

    // お気に入りの変数を定義
    private Like mLike;

    // ログイン済みのユーザーを取得する
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // お気に入りボタンの設定
        mLikeButton = (Button)findViewById(R.id.like_button);

        if (user == null) {
            // ログインしていなければ、お気に入りボタンを非表示
            mLikeButton.setVisibility(View.INVISIBLE);
        } else {
            // ログインしていれば、お気に入りボタンを表示
            mLikeButton.setVisibility(View.VISIBLE);
        }

        // お気に入りボタンをクリックした時の処理
        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            // データベース(Constファイル)にお気に入りを保存
            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            // Firebaseから質問がお気に入り登録されているかのデータを取得。お気に入りされているユーザーIDの質問を取得し、変数に代入。
            DatabaseReference likeRef = dataBaseReference.child(Const.LikesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            //DatabaseReference likeRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.LikesPATH);

                Map<String, String> data = new HashMap<String, String>();

                if (likeRef != null) {

                    // UIDをFirebaseに登録
                    data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    // 表示名
                    // Preferenceから名前を取る
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String name = sp.getString(Const.NameKEY, "");
                    // nameを保存
                    data.put("name", name);


                    // お気に入りを保存
                    // String like = sp.getString(Const.LikesPATH, "");
                    // data.put("like", like);

                    // 質問を保存
                    // String content = sp.getString(Const.ContentsPATH, "");
                    // data.put("content", content);

                    // ボタンのTextを「解除」に変更
                    mLikeButton.setText("解除");
                    // お気に入りのdataをFirebaseに保存
                    likeRef.setValue(data);
                }
                else {
                    // ボタンのTextを「お気に入り」に変更
                    mLikeButton.setText("お気に入り");
                    // お気に入りのdataをFirebaseに保存
                    likeRef.removeValue();
                }
            }
        });

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}