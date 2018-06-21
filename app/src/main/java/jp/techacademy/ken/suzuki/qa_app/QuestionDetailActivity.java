package jp.techacademy.ken.suzuki.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    // private ImageView mImageView;

    // お気に入りボタンの変数を定義
    private Button mLikeButton;

    // 画面を表示した時に、isLikeにお気に入りかどうかというデータをGetterメソッドによって保持。（フィールドがboolean型の場合、Getterメソッドの名前をis + フィールド名とします。）
    private boolean isLike = false;

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

    // データに追加・変化があった時に受け取るChildEventListenerを作成
    private ChildEventListener mFavoriteListener = new ChildEventListener() {

        // Firebaseにお気に入りが追加された時に呼ばれるメソッド
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            isLike = true;
            if (mLikeButton != null) {
                mLikeButton.setText("解除");
            }
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

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        // お気に入りボタンの設定
        mLikeButton = (Button)findViewById(R.id.like_button);

        if (user == null) {
            // ログインしていなければ、お気に入りボタンを非表示
            mLikeButton.setVisibility(View.INVISIBLE);
        } else {
            // ログインしていれば、お気に入りボタンを表示
            mLikeButton.setVisibility(View.VISIBLE);
        }

        // Firebaseを参照
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();

        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // ログインしている時の質問詳細画面への処理
        if (user != null){

        // Firebaseからユーザーにお気に入りされている質問を likeRef 変数に代入。
        final DatabaseReference likeRef = dataBaseReference.child(Const.LikesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
        // Firebaseにお気に入りを追加するイベントリスナーを定義
        likeRef.addChildEventListener(mFavoriteListener);

        // お気に入りボタンをクリックした時の処理
        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> data = new HashMap<String, String>();


                // isLikeがfalseなら（お気に入りされていないなら）
                if (!isLike) {

                    // UIDをFirebaseに登録
                    data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    // 表示名
                    // Preferenceから名前を取る
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String name = sp.getString(Const.NameKEY, "");
                    // nameをFirebaseに登録
                    data.put("name", name);

                    // 添付画像を取得する
                    // mImageView = (ImageView) findViewById(R.id.imageView);
                    // BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

                    // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
                    // if (drawable != null) {
                        //Bitmap bitmap = drawable.getBitmap();
                        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        //String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                        //data.put("image", bitmapString);
                    // }

                    // ボタンのTextを「解除」に変更
                    mLikeButton.setText("解除");
                    // Firebaseにお気に入りを追加
                    likeRef.setValue(data);
                    // isLike 変数にお気に入りを追加
                    isLike = true;
                }
                else {
                    // ボタンのTextを「お気に入り」に変更
                    mLikeButton.setText("お気に入り");
                    // Firebaseからお気に入りを削除
                    likeRef.removeValue();
                    // isLike 変数からお気に入りを削除
                    isLike = false;
                }
            }
        });
        }

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

        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}