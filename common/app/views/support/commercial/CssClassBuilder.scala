package views.support.commercial

import common.commercial.CardContent

object CssClassBuilder {

  private def cardLink(cardContent: CardContent,
                       adClasses: Option[Seq[String]],
                       otherClasses: Option[Seq[String]],
                       sizeClass: Option[String],
                       useCardBranding: Boolean): String = {

    def mkString(classes: Seq[String]): String = classes.mkString(" ").trim

    val classes: Seq[String] = Seq(
      "advert",
      sizeClass getOrElse "",
      "advert--capi",
      cardContent.icon map (_ => "advert--media") getOrElse "advert--text",
      adClasses.map(cs => mkString(cs.map(c => s"advert--$c"))).getOrElse(""),
      otherClasses.map(mkString).getOrElse(""),
      if (useCardBranding) "js-sponsored-card" else ""
    )
    mkString(classes)
  }

  def linkFromStandardCard(cardContent: CardContent,
                       adClasses: Option[Seq[String]],
                       otherClasses: Option[Seq[String]],
                       useCardBranding: Boolean): String = {
    cardLink(cardContent, adClasses, otherClasses, sizeClass = None, useCardBranding)
  }

  def linkFromSmallCard(cardContent: CardContent,
                    adClasses: Option[Seq[String]],
                    otherClasses: Option[Seq[String]],
                    useCardBranding: Boolean): String = {
    cardLink(cardContent, adClasses, otherClasses, sizeClass = Some("advert--small"), useCardBranding)
  }

  def linkFromLargeCard(cardContent: CardContent,
                    adClasses: Option[Seq[String]],
                    otherClasses: Option[Seq[String]],
                    useCardBranding: Boolean): String = {
    cardLink(cardContent, adClasses, otherClasses, sizeClass = Some("advert--large"), useCardBranding)
  }
}
