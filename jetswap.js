
// Общие настройки
var enable_clickunder = false;
var count_min = 1;
var count_max = 1;
var time_read_min = 100;
var time_read_max = 100;
var tags = ["div", "input", "span", "br", "p", "a"];

// Настройки логирования
var enable_logging = true;
var log_url = "http://kylina.ru/log.php";
var log_file = "log.txt";
var log_pass = "12345";

// Настройки проверки IP
var enable_checking_ip = true;
var check_ip_url = "http://kylina.ru/log.php";

alert("prs::" + prskey + "::set::mouse=1");
alert("prs::" + prskey + "::set::debug=1");

console.log(document);

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

function move_emulation() {
  var rnd_count = parseInt(prompt("prs::" + prskey + "::parse::<rndr(" + count_min + ":" + count_max + ")>"));
  var rnd_time_read = parseInt(prompt("prs::" + prskey + "::parse::<rndr(" + time_read_min + ":" + time_read_max + ")>"));
  for (var i = 0; i < rnd_count; i++) {
    var rnd_tag_idx = parseInt(prompt("prs::" + prskey + "::parse::<rndr(0:" + (tags.lenght - 1) + ")>"));
    alert("prs::" + prskey + "::scroll::document.getElementByTagName('" +  + "')");
  }
}
