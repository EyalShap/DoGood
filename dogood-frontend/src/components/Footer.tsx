import React from 'react';
import './../css/Footer.css'

const Footer: React.FC = () => {
  return (
    <footer className="footer">
      <div className="social-container">
        <a href="https://www.instagram.com" target="_blank" rel="noopener noreferrer" className="social-link">
          <img src='https://banner2.cleanpng.com/20180409/zjw/avgxtga41.webp' alt="Instagram" className="social-icon" />
        </a>
        <a href="https://www.facebook.com" target="_blank" rel="noopener noreferrer" className="social-link">
          <img src='https://w7.pngwing.com/pngs/806/294/png-transparent-facebook-logo-logo-facebook-icon-facebook-logo-brand-social-network-scalable-vector-graphics.png' alt="Facebook" className="social-icon" />
        </a>
        <a href="https://www.twitter.com" target="_blank" rel="noopener noreferrer" className="social-link">
          <img src='https://w7.pngwing.com/pngs/748/680/png-transparent-twitter-x-logo.png' alt="Twitter" className="social-icon" />
        </a>
      </div>
    </footer>
  );
};

export default Footer;
