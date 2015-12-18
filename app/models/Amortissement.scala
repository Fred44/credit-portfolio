package models

import org.joda.time.{PeriodType, DateTime}
import com.github.nscala_time.time.Imports._

import scala.math.BigDecimal.RoundingMode

/**
 * Created by fred on 06/04/15.
 */
case class Amortissement (credit: Credit, operations: Seq[Operation] = Nil) {

  val echeances = calcul (1, credit.depart, credit.capital)

  private def calcul (n: Int, date: DateTime, k: BigDecimal): List[Echeance] = {
    val remb = operations collect {
      case RemboursementOperation (d, f, m) if d == date => m
    } sum
    val m = mensualiteAdate (date) + remb
    val t = tauxAdate (date)
    val r = t / 1200
    val i = (k * r).setScale(2, RoundingMode.HALF_UP)
    val ass = credit.assurance.base match {
      case "CI" =>
        credit.capital * credit.assurance.taux / 1200
      case "CRD" =>
        k * credit.assurance.taux / 1200
    }
    val a = if (m - i - ass > k) k else m - i - ass

    if (k > 0)
      Echeance (
        num = n, date = date,
        mensualite = if (m - i - ass > k) k else m,
        interets = i, amortissement = a,
        capital = k - a,
        taux = t,
        assurance = ass
      ) :: calcul (n + 1, date + 1.month, k - a)
    else
      Nil
  }

  private def mensualiteAdate (date: DateTime): BigDecimal =
    (operations sortBy { _.date } collect {
      case MensualiteOperation (d, f, m) if d <= date => m
    } lastOption).getOrElse(credit.mensualite)

  private def tauxAdate (date: DateTime): BigDecimal =
    (operations sortBy { _.date } collect {
      case TauxOperation (d, f, t) if d <= date => t
    } lastOption).getOrElse (credit.taux)

  /**
   * Imprimer à l'écran le tableau d'amortissament synthétisé par année.
   */
  def amortissementSynth =
    echeances
      .groupBy (_.date.year)
      .map { case (year, echeances) =>
      echeances.reduce { (a, b) =>
        Echeance(1, b.date, a.mensualite + b.mensualite, a.interets + b.interets, a.amortissement + b.amortissement, b.capital, b.taux, a.assurance + b.assurance)
      }
    }
      .toSeq.sortBy (_.date)
      .zipWithIndex.map { case (e, i) =>
      Echeance(i+1, e.date, e.mensualite, e.interets, e.amortissement, e.capital, e.taux, e.assurance)
    }

  /**
   * Obtenir l'échéance pour une date donnée.
   * @param date
   * @return
   */
  def echeance (date: DateTime = DateTime.now): Echeance = {
    val echCnt = if (credit.depart <= date) (credit.depart to date).toPeriod(PeriodType.months()).months else 0

    if (echCnt <= echeances.size) echeances (echCnt)
    else echeances.last
  }

  /**
   * @return
   *         la date de fin du crédit
   */
  def fin = echeances.last.date

  /**
   * @return
   *         le nombre d'échéances du crédit
   */
  def dureeEffective = echeances.size

  /**
   * @return
   *         le nombre d'échéances restantes
   */
  def dureeRestante (date: DateTime = DateTime.now) =
    date match {
      case d if (d < credit.depart) =>
        dureeEffective
      case d if (d > fin) =>
        0
      case _ =>
        dureeEffective - echeance (date).num
    }

  /**
   * @param date
   * @return
   *         le capital amorti à date
   */
  def amorti (date: DateTime = DateTime.now) =
    date match {
      case d if (d < credit.depart) =>
        BigDecimal(0.0)
      case d if (d > fin) =>
        credit.capital
      case _ =>
        credit.capital - echeance (date).capital
    }

  /**
   * @param date
   * @return
   *         le capital restant du
   */
  def reste (date: DateTime = DateTime.now) =
    credit.capital - amorti(date)

  /**
   * Caclcul du cout du crédit (total intérêts versés)
   * @return
   */
  def cout (date: DateTime = fin) = {
    if (date < credit.depart) BigDecimal(0)
    else (echeance(date) match {
      case e =>
        echeances slice(0, e.num) map { e => e.interets + e.assurance } sum
    }) + (operations collect {
      case o: Operation if o.date <= date => o.frais
    } sum)
  }

  def progress = {
    val total = dureeEffective
    val effectuee = total - dureeRestante()

    effectuee * 100 / total
  }

  override def toString = {
    val last = echeances.last

    f"""Capital : ${credit.capital}%,16.2f €
       |Taux :    ${last.taux}%16.2f %%
       |Durée :   $dureeEffective%16d m
       |Coût :    ${cout()}%,16.2f €
       |Total :   ${credit.capital + cout()}%,16.2f €
       |Commence :${credit.depart.toString("dd-MM-yyyy")}%16s
       |Termine : ${last.date.toString("dd-MM-yyyy")}%16s
     """.stripMargin
  }
}
