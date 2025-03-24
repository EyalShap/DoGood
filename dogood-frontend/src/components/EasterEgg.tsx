import React from 'react';

const EasterEgg: React.FC = () => {
  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100vw',
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'black',
        zIndex: 9999,
      }}
    >
      <iframe
        style={{
          width: '100vw',
          height: '100vh',
          border: 'none',
        }}
        src="https://www.youtube.com/embed/dQw4w9WgXcQ?autoplay=1&mute=1"
        title="YouTube video player"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share; fullscreen"
      ></iframe>
    </div>
  );
};

export default EasterEgg;
