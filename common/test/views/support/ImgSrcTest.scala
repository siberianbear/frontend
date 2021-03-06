package views.support

import com.gu.contentapi.client.model.v1.{Asset, Content, Element, Tag, AssetType, ElementType, TagType}
import com.gu.contentapi.client.utils.CapiModelEnrichment.RichJodaDateTime
import conf.Configuration
import conf.switches.Switches.ImageServerSwitch
import model.{ImageMedia, ImageAsset}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.play.OneAppPerSuite

class ImgSrcTest extends FlatSpec with Matchers with OneAppPerSuite {

  lazy val imageHost = Configuration.images.path

  val asset = Asset(
    AssetType.Image,
    Some("image/jpeg"),
    Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2013/7/5/1373023097878/b6a5a492-cc18-4f30-9809-88467e07ebfa-460x276.jpeg"),
    None
  )

  val element = Element("elementId", "main", ElementType.Image, Some(1), List(asset))

  val tag = List(Tag(id = "type/article", `type` = TagType.Keyword, webTitle = "",
      sectionId = None, sectionName = None, webUrl = "", apiUrl = "apiurl", references = Nil))

  val content = Content(id = "foo/2012/jan/07/bar",
    sectionId = None,
    sectionName = None,
    webPublicationDate = Some(new DateTime().toCapiDateTime),
    webTitle = "Some article",
    webUrl = "http://www.guardian.co.uk/foo/2012/jan/07/bar",
    apiUrl = "http://content.guardianapis.com/foo/2012/jan/07/bar",
    tags = tag,
    elements = Some(List(element))
  )

  val imageAsset = ImageAsset.make(asset, 1)

  val image = ImageMedia.apply(Seq(imageAsset))

  val mediaImageAsset = ImageAsset.make(Asset(
    AssetType.Image,
    Some("image/jpeg"),
    Some("http://media.guim.co.uk/knly7wcp46fuadowlsnitzpawm/437_0_3819_2291/1000.jpg"),
    None
  ), 1)

  val mediaImage = ImageMedia.apply(Seq(mediaImageAsset))

  val s3UploadJpgImageAsset = ImageAsset.make(Asset(
    AssetType.Image,
    Some("image/jpeg"),
    Some("https://uploads.guim.co.uk/2016/02/10/Screen_Shot_2016-02-09_at_17.50.09.jpeg"),
    None
  ), 1)

  val s3UploadJpgImage = ImageMedia.apply(Seq(s3UploadJpgImageAsset))

  val s3UploadPNGImageAsset = ImageAsset.make(Asset(
    AssetType.Image,
    Some("image/png"),
    Some("https://uploads.guim.co.uk/2016/02/04/gu.png"),
    None
  ), 1)

  val s3UploadPNGImage = ImageMedia.apply(Seq(s3UploadPNGImageAsset))

  val s3UploadGifImageAsset = ImageAsset.make(Asset(
    AssetType.Image,
    Some("image/gif"),
    Some("https://uploads.guim.co.uk/2016/02/04/meep.gif"),
    None
  ), 1)

  val s3UploadGifImage = ImageMedia.apply(Seq(s3UploadGifImageAsset))

  "ImgSrc" should "convert the URL of a static image to the resizing endpoint with a /static prefix" in {
    ImageServerSwitch.switchOn()
      Item700.bestFor(image) should be (Some(s"$imageHost/img/static/sys-images/Guardian/Pix/pictures/2013/7/5/1373023097878/b6a5a492-cc18-4f30-9809-88467e07ebfa-460x276.jpeg?w=700&q=55&auto=format&usm=12&fit=max&s=1e7d3c46056f162deec921e3c21de647"))
  }

  it should "convert the URL of a media service to the resizing endpoint with a /media prefix" in {
    ImageServerSwitch.switchOn()
      Item700.bestFor(mediaImage) should be (Some(s"$imageHost/img/media/knly7wcp46fuadowlsnitzpawm/437_0_3819_2291/1000.jpg?w=700&q=55&auto=format&usm=12&fit=max&s=278bfb59ec002e4ffe05d4ee548c4135"))
  }

  it should "not convert the URL of the image if it is disabled" in {
    ImageServerSwitch.switchOff()
    Item700.bestFor(image) should be (Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2013/7/5/1373023097878/b6a5a492-cc18-4f30-9809-88467e07ebfa-460x276.jpeg"))
  }

  it should "convert the URL of the image if it is a PNG (original image from static.guim.co.uk domain)" in {
    ImageServerSwitch.switchOn()
    val pngImage = ImageMedia.apply(Seq(ImageAsset.make(asset.copy(file = Some("http://static.guim.co.uk/sys-images/Guardian/Pix/contributor/2014/10/30/1414675415419/Jessica-Valenti-R.png")),0)))
    Item700.bestFor(pngImage) should be (Some(s"$imageHost/img/static/sys-images/Guardian/Pix/contributor/2014/10/30/1414675415419/Jessica-Valenti-R.png?w=700&q=55&auto=format&usm=12&fit=max&s=e0bbacaf2f991bf660e22d4983b891ca"))
  }

  it should "convert the URL of the image if it is a PNG (original image from static-secure.guim.co.uk domain)" in {
    ImageServerSwitch.switchOn()
    val pngImage = ImageMedia.apply(Seq(ImageAsset.make(asset.copy(file = Some("http://static-secure.guim.co.uk/sys-images/Guardian/Pix/contributor/2014/10/30/1414675415419/Jessica-Valenti-R.png")),0)))
    Item700.bestFor(pngImage) should be (Some(s"$imageHost/img/static/sys-images/Guardian/Pix/contributor/2014/10/30/1414675415419/Jessica-Valenti-R.png?w=700&q=55&auto=format&usm=12&fit=max&s=e0bbacaf2f991bf660e22d4983b891ca"))
  }

  it should "not convert the URL of the image if it is a GIF (we do not support animated GIF)" in {
    ImageServerSwitch.switchOn()
    val gifImage = ImageMedia.apply(Seq(ImageAsset.make(asset.copy(file = Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2013/7/5/1373023097878/b6a5a492-cc18-4f30-9809-88467e07ebfa-460x276.gif")),0)))
    Item700.bestFor(gifImage) should be (Some("http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2013/7/5/1373023097878/b6a5a492-cc18-4f30-9809-88467e07ebfa-460x276.gif"))
  }

  it should "not convert the URL of the image if it is not one of ours" in {
    ImageServerSwitch.switchOn()
    val someoneElsesImage = ImageMedia(Seq(ImageAsset.make(asset.copy(file = Some("http://foo.co.uk/sys-images/Guardian/Pix/pictures/2013/7/5/1373023097878/b6a5a492-cc18-4f30-9809-88467e07ebfa-460x276.gif")),0)))
    Item700.bestFor(someoneElsesImage) should be (Some("http://foo.co.uk/sys-images/Guardian/Pix/pictures/2013/7/5/1373023097878/b6a5a492-cc18-4f30-9809-88467e07ebfa-460x276.gif"))
  }

  it should "convert the URL of a jpeg s3 upload to the resizing endpoint with a /uploads prefix" in {
    ImageServerSwitch.switchOn()
    Item700.bestFor(s3UploadJpgImage) should be (Some(s"$imageHost/img/uploads/2016/02/10/Screen_Shot_2016-02-09_at_17.50.09.jpeg?w=700&q=55&auto=format&usm=12&fit=max&s=2058cee6d51d04886bdf5d179fa900b0"))
  }

  it should "convert the URL of a png s3 upload to the resizing endpoint with a /uploads prefix" in {
    ImageServerSwitch.switchOn()
    Item700.bestFor(s3UploadPNGImage) should be (Some(s"$imageHost/img/uploads/2016/02/04/gu.png?w=700&q=55&auto=format&usm=12&fit=max&s=16cc94b0e0a1c8081cf377da5c14ee51"))
  }

  it should "not convert the URL of a gif s3 upload (we do not support animated GIF)" in {
    ImageServerSwitch.switchOn()
    Item700.bestFor(s3UploadGifImage) should be (Some(s"https://uploads.guim.co.uk/2016/02/04/meep.gif"))
  }
}
