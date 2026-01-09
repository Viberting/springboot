//该文件用于存放一些会经常复用的功能函数，以便任意vue文件中可以访问这些函数
import axios from 'axios'

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

// 添加POST请求方法
export function post(url, data = {}) {
  return axios.post(url, data, {
    withCredentials: true
  });
}

// 添加GET请求方法
export function get(url) {
  return axios.get(url, {
    withCredentials: true
  });
}

// 关键：导出供其他模块使用的函数，名称与使用处保持一致
export {
  undefine,
  nullZeroBlank,
  notNullZeroBlank,
  dateFormat
};

/**
 * 转换为整数，失败则返回默认值
 * @param {any} value - 要转换的值
 * @param {number} defaultValue - 默认值（默认0）
 * @returns {number} 转换后的整数
 */
export function toInt(value, defaultValue = 0) {
  const num = parseInt(value);
  return isNaN(num) ? defaultValue : num;
}

/**
 * 校验参数是否为有效数字（大于0）
 * @param {any} value - 要校验的值
 * @returns {boolean} 是否有效
 */
export function isValidNumber(value) {
  const num = toInt(value);
  return num > 0;
}

/**
 * 清理对象中的undefined/null值
 * @param {object} obj - 要清理的对象
 * @returns {object} 清理后的对象
 */
export function cleanObject(obj) {
  const result = {};
  for (const key in obj) {
    const value = obj[key];
    if (value !== undefined && value !== null && value !== '') {
      result[key] = value;
    }
  }
  return result;
}