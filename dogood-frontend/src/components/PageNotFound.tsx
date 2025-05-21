import snail from "/snail.png";

function PageNotFound() {
    return <div style={{
      textAlign: "center",
      padding: "0px 60px 20px",
      fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif"
    }}>
        <h1 style={{
        fontSize: "64px",
        marginBottom: "20px",
        color: "#008B8B"
      }}>Not (Do) Good</h1>
        <h2 style={{
        fontSize: "28px",
        fontWeight: "normal",
        color: "#666"
      }}>Page Not Found</h2>
      <img src={snail}></img>
    </div>
}

export default PageNotFound