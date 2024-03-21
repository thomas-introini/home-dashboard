new Chart(document.getElementById("chart"), {
  type: "line",
  data: {
    labels: [%s],
    datasets: [
      {
        label: "Temperature",
        data: [%s],
        borderWidth: 1,
      },
      {
        label: "Humidity",
        data: [%s],
        borderWidth: 1,
      },
    ],
  },
  options: {
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  },
});
