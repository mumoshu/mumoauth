package com.github.mumoshu.oauth

trait Response {
  def status: Int
  def contentType: Option[String]
  def body: Option[String]
  def location: Option[String]
  def httpVersion: String
}
