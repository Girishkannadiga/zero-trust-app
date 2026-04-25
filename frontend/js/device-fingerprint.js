const DeviceFingerprint = (() => {

  function getScreenInfo() {
    return `${screen.width}x${screen.height}x${screen.colorDepth}`;
  }

  function getTimezone() {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  }

  function getLanguage() {
    return navigator.language || navigator.userLanguage || 'unknown';
  }

  function getPlatform() {
    return navigator.platform || 'unknown';
  }

  function getPluginCount() {
    return navigator.plugins ? navigator.plugins.length : 0;
  }

  function getCookieEnabled() {
    return navigator.cookieEnabled;
  }

  function getDoNotTrack() {
    return navigator.doNotTrack || window.doNotTrack || 'unset';
  }

  async function hashString(str) {
    const encoder = new TextEncoder();
    const data = encoder.encode(str);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  }

  async function generate() {
    const components = [
      navigator.userAgent,
      getScreenInfo(),
      getTimezone(),
      getLanguage(),
      getPlatform(),
      getPluginCount(),
      getCookieEnabled(),
      getDoNotTrack(),
    ].join('|');

    const fingerprint = await hashString(components);
    return fingerprint;
  }

  function getDeviceLabel() {
    const ua = navigator.userAgent;
    if (/mobile/i.test(ua)) return 'Mobile Browser';
    if (/tablet|ipad/i.test(ua)) return 'Tablet Browser';
    return 'Desktop Browser';
  }

  function getOSInfo() {
    const ua = navigator.userAgent;
    if (/windows nt 10/i.test(ua)) return 'Windows 10/11';
    if (/windows/i.test(ua))       return 'Windows';
    if (/mac os x/i.test(ua))      return 'macOS';
    if (/linux/i.test(ua))         return 'Linux';
    if (/android/i.test(ua))       return 'Android';
    if (/iphone|ipad/i.test(ua))   return 'iOS';
    return 'Unknown OS';
  }

  function getBrowserInfo() {
    const ua = navigator.userAgent;
    if (/edg\//i.test(ua))    return 'Microsoft Edge';
    if (/chrome/i.test(ua))   return 'Google Chrome';
    if (/firefox/i.test(ua))  return 'Mozilla Firefox';
    if (/safari/i.test(ua))   return 'Apple Safari';
    if (/opera/i.test(ua))    return 'Opera';
    return 'Unknown Browser';
  }

  async function getFullProfile() {
    const fingerprint = await generate();
    return {
      fingerprint,
      label:    getDeviceLabel(),
      os:       getOSInfo(),
      browser:  getBrowserInfo(),
      screen:   getScreenInfo(),
      timezone: getTimezone(),
      language: getLanguage(),
    };
  }

  return { generate, getFullProfile, getDeviceLabel, getOSInfo, getBrowserInfo };
})();
