//该文件用于存放一些会经常复用的功能函数，以便任意vue文件中可以访问这些函数
function undefine(i) {
  return typeof i === 'undefined'
}

function nullZeroBlank(i) {
  // true 表示参数为 null / undefined / 空字符串（只含空白） / 0
  if (i == null) return true
  if (typeof i === 'string') {
    // 把连续空白缩成一个并去除首尾空白
    const str = i.replace(/\s+/g, ' ').trim()
    if (str.length === 0) return true
  }
  if (i === 0) return true
  return false
}

function notNullZeroBlank(i) {
  return !nullZeroBlank(i)
}

function dateFormat(dateString, format) {
  try{  //javascript也支持try语句捕获异常
    let date = new Date(dateString);//根据字符串生成Date对象
    if ("yyyy-MM-dd" === format) {
      let dateFormat = date.getFullYear() + "-";
      dateFormat+=date.getMonth()+"-";
      dateFormat+=date.getDate();
      return dateFormat;
    }else{
      return "无此格式！"
    }
  }catch(e){
    return "格式转换错误！"
  }
  
}

// 关键：导出供其他模块使用的函数，名称与使用处保持一致
export {
  undefine,
  nullZeroBlank,
  notNullZeroBlank,
  dateFormat
}