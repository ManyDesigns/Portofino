page.customButtonAction = function () {
  alert("Custom button clicked!");
};
page.declareButton({ list: "search-results", text: "Custom button" }, 'customButtonAction');
