
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

//выводим информацию о показе первого блока на вкладку Журнал программы тестирования.
alert("prs::" + prskey + "::debug::Running First Block");

//задаем параметры команд

var referer=prompt("prs::" + prskey + "::parse::http://<rndt(jethosting.ru!!regjet.ru!!reg.ru!!nic.ru)>"); //список рефереров
var cmdname="nav"; //команда переход
var cmdtime=parseInt(prompt("prs::" + prskey + "::parse::<rndr(20:30)>")); //время показа команды от 20 до 30 секунд.
var cmdparam="http://regjet.ru<referer(" + referer + ")>"; //параметры команды

//добавляем команду команду в презентацию. если удачно - команда появится на вкладке "Информация" программы тестирования презентации.
alert("prs::" + prskey + "::add::" + cmdname + "::" + cmdtime + "::" + cmdparam);

var cmdname="link"; //команда поиск ссылки
var cmdtime=5;//это последняя команда, время значения не имеет.
var cmdparam="link;regjet;-1"; //поиск случайной ссылки, содержащей regjet
alert("prs::" + prskey + "::add::" + cmdname + "::" + cmdtime + "::" + cmdparam);

alert("prs::" + prskey + "::set::cmdindex=1;cmdtime=0;");

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
