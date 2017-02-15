package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;

/**
 * <p>Title: MetNeuroScore</p>
 *
 * <p>Description: Neural scoring subsystem</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: Vsetech</p>
 *
 * @author Fedd Kraft
 * @version 0.1
 */
public class FullStatistic extends Statistic {
    // статистика сигналов
    public long askCnt=0;// количество сигналов, прошедших через нейрон или синапс
    public double askAvg=0;// средняя величина сигнала
    public double askAvgPosDev=0;// средняя положительная разница между средним значением и проходящими значениями
    public double askAvgNegDev=0;// средняя отрицательная разница между средним значением и проходящими значениями
    public long askPosDevCnt=0;// количество положительных отклонений

    // статистика тестов без поучений
    public long errCnt=0;// количество проверок (в том числе с дальнейшими поучениями)
    public double errAvg=0;// средняя величина ошибки (средняя разница между данным и правильным ответами)
    public double errAvgPosDev=0; // средняя положительная разница между средней ошибкой и всеми ошибками
    public long errPosDevCnt=0;// количество положительных отклонений
    public double errAvgNegDev=0; // средняя отрицательная разница между средней ошибкой и всеми ошибками

    public synchronized void recordError(double error){

        // регистрируем ошибку в статистике последних ошибок
        //super.recordError(error);

        // регистрируем остальную статистику
        // изменяем среднюю разницу
        this.errAvg = (this.errAvg * this.errCnt + error) /
                                (this.errCnt + 1);
        //считаем среднее отклонение
        double dev = this.errAvg - error;
        if (dev > 0) {
            // положительное
            this.errAvgPosDev =
                    (this.errAvgPosDev * this.errPosDevCnt + dev) /
                                          (this.errPosDevCnt + 1);

            this.errPosDevCnt++;
        } else {
            // отрицательное
            double errNegDevCnt = this.errCnt -
                                  this.errPosDevCnt;

            this.errAvgNegDev = (this.errAvgNegDev *
                                           errNegDevCnt + dev) /
                                          (errNegDevCnt + 1);
        }
        this.errCnt++;
    }




    public synchronized void recordAsking(double question){
        //log.trace("start record asking statistic");
        // изменяем средний сигнал
        this.askAvg = (this.askAvg * this.askCnt + question) / (this.askCnt + 1);
        //считаем среднее отклонение
        double dev = this.askAvg - question;
        if(dev>0){
            // положительное
            this.askAvgPosDev = (this.askAvgPosDev * this.askPosDevCnt + dev) / (this.askPosDevCnt + 1);

            this.askPosDevCnt++;
        } else {
            double askNegDevCnt = this.askCnt-this.askPosDevCnt;
            // отрицательное
            this.askAvgNegDev = (this.askAvgNegDev * askNegDevCnt + dev) / (askNegDevCnt + 1);
        }
        this.askCnt++;
    }

    public void recordTeaching(double correction) {
    }
}
