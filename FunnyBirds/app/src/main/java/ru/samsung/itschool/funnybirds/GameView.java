package ru.samsung.itschool.funnybirds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class GameView extends View{

    private Sprite playerBird;
    private Sprite enemyBird;
    private Sprite enemyBoom;
    private Sprite bonus;

    private int lvl;
    private int viewWidth;
    private int viewHeight;

    private int points = 0;

    private final int timerInterval = 30;

    private boolean stoped = false;
    private boolean won = false;
    public boolean paused = false;
    public boolean resumed = false;

    public GameView(Context context) {
        super(context);
        lvl=0;


        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        int w = b.getWidth()/5;
        int h = b.getHeight()/3;
        Rect firstFrame = new Rect(0, 0, w, h);
        playerBird = new Sprite(10, 0, 0, 100, firstFrame, b);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i ==0 && j == 0) {
                    continue;
                }
                if (i ==2 && j == 3) {
                    continue;
                }
                playerBird.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }

        b = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);
        w = b.getWidth()/5;
        h = b.getHeight()/3;
        firstFrame = new Rect(4*w, 0, 5*w, h);

        enemyBird = new Sprite(2000, 250, -300, 0, firstFrame, b);

        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {

                if (i ==0 && j == 4) {
                    continue;
                }

                if (i ==2 && j == 0) {
                    continue;
                }

                enemyBird.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }

        b = BitmapFactory.decodeResource(getResources(), R.drawable.bonus);
        w = b.getWidth()/5;
        h = b.getHeight()/3;
        firstFrame = new Rect(4*w, 0, 5*w, h);

        bonus = new Sprite(2000, 250, -300, 0, firstFrame, b);

        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {

                if (i ==0 && j == 4) {
                    continue;
                }

                if (i ==2 && j == 0) {
                    continue;
                }

                bonus.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }



        Timer t = new Timer();
        t.start();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(250, 127, 199, 255);
        playerBird.draw(canvas);
        enemyBird.draw(canvas);
        bonus.draw(canvas);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(55.0f);
        p.setColor(Color.WHITE);
        canvas.drawText(points + "", viewWidth - 200, 70, p);
        canvas.drawText(lvl + "", viewWidth - 400, 70, p);

        if (stoped==true){
            p.setTextSize(100.0f);
            canvas.drawText("GAME OVER.", getWidth()/4, getHeight()/2, p);
        }

        if (won==true){
            p.setTextSize(100.0f);
            canvas.drawText("YOU WIN!", getWidth()/4, getHeight()/2, p);
        }
        if (paused==true){
            p.setTextSize(100.0f);
            canvas.drawText("PAUSE", getWidth()/2, getHeight()/2, p);
        }

    }

    protected void update () {

        if (paused==false) {
            playerBird.update(timerInterval);
            enemyBird.update(timerInterval);
            bonus.update(timerInterval);
        }

        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVy(-playerBird.getVy());
            points--;
        }
        else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVy(-playerBird.getVy());
            points--;
        }

        if (enemyBird.getX() < - enemyBird.getFrameWidth()) {
            teleportEnemy(enemyBird);
            points +=5;
        }

        if (enemyBird.intersect(playerBird)) {
            teleportEnemy (enemyBird);
            points -= 15;
        }

        if (bonus.getX() < - bonus.getFrameWidth()) {
            teleportEnemy(bonus);
        }

        if (bonus.intersect(playerBird)) {
            teleportEnemy (bonus);
            points += 25;
        }

        if(points>=30){
            teleportEnemy(enemyBird);
            teleportEnemy(bonus);
            lvl+=1;
            setLvl();
            points=0;
        }
        if(points<=-30){
            stopGame();
            stoped=true;
        }
        if(lvl>=20) {
            stopGame();
            won = true;
        }
        if (paused==true){
            resumed=false;
            pauseGame();
        }

        if(resumed==true){
            setLvl();
            resumed=false;
        }



        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN && paused == false)  {

            if (event.getY() < playerBird.getBoundingBoxRect().top) {
                playerBird.setVy(-100-(15*lvl));
                points--;
            }
            else if (event.getY() > (playerBird.getBoundingBoxRect().bottom)) {
                playerBird.setVy(100+(15*lvl));
                points--;
            }
        }

        return true;
    }

    private void setLvl(){
        enemyBird.setVx(-300-lvl*100);
        bonus.setVx(-300-lvl*100);
        playerBird.setVy(100+lvl*15);
    }

    private void stopGame(){
        enemyBird.setVx(0);
        bonus.setVx(0);
        points=0;
        lvl=0;
        playerBird.setVx(0);
        enemyBird.setX(viewHeight+10000);
        bonus.setX(viewHeight+10000);
        playerBird.setX(viewHeight+10000);
    }

    private void pauseGame(){
        enemyBird.setVx(0);
        bonus.setVx(0);
        playerBird.setVy(0);
    }


    private void teleportEnemy (Sprite sprite) {
        sprite.setX(viewWidth + Math.random() * 500);
        sprite.setY(Math.random() * (viewHeight - enemyBird.getFrameHeight()));
    }


    class Timer extends CountDownTimer {

        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            update ();
        }

        @Override
        public void onFinish() {

        }
    }
}
