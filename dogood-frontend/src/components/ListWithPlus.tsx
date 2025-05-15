import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ListProps } from "./ListWithArrows";
import './../css/ListWithPlus.css'
import './../css/CommonElements.css'

const List: React.FC<ListProps> = ({ data, limit, navigateTo }) => {
  const navigate = useNavigate();
  const [currentLimit, setCurrentLimit] = useState(limit); 

  const handlePlus = () => {
    setCurrentLimit(currentLimit + limit);
  };

  const handleTitleOnClick = (postId: string | number) => {
    navigate(`/${navigateTo}/${postId}`);
  }

  const visibleItems = data.slice(0, currentLimit);

  return (
    <div className="plusListComponent">
      <link
      href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
      rel="stylesheet"
      />

      <div className="listWithPlus">
        {visibleItems.map((item) => (
          <div key={item.id} className="plusListItem" onClick={() => handleTitleOnClick(item.id)}>
            <img
              src={item.image}
              alt={item.title}
              className="plusListItemImg"
            />
            <h3 className="plusListItemHeader" style={{overflowWrap: "break-word"}}>{item.title}</h3>
            <p className="plusListItemDesc" style={{overflowWrap: "break-word"}}>{item.description}</p>
          </div>
        ))}
      </div>

      {currentLimit < data.length && (
        <button
          onClick={handlePlus}
          className="plusButton"
        >
        </button>
      )}

    </div>
  );
};

export default List;