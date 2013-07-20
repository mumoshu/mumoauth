package oauth2

trait ScopeDefinition {
  /**
   * @see http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-3.3
   * @see <code>0x21 :: (0x23 to 0x5b toList) ++ (0x5d to 0x7e) map ("\"" + _.toChar + "\"") mkString (", ")</code>
   */
  val allowedCharacters = Set(
    "!", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/",
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@",
    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
    "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
    "[", "]", "^", "_", "`",
    "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
    "r", "s", "t", "u", "v", "w", "x", "y", "z",
    "{", "|", "}", "~"
  )
  def Values(scopes: Scope*): Map[String, Scope] = scopes.map(s => s.scope -> s).toMap
  def find(scope: String): Option[Scope] = values.get(scope)

  def values: Map[String, Scope]
}
