import { useState } from "react";
import './../css/ListWithArrows.css'
import './../css/CommonElements.css'
import { useNavigate } from "react-router-dom";

export interface ListItem {
  id: number | string;
  image: string;
  title: string;
  description: string;
}

export interface ListProps {
  data: ListItem[];
  limit: number;
  navigateTo: string;
  clickable?: (id: number | string) => boolean;
  onRemove? : (arg0: string) => Promise<void>;
  isOrgManager? : boolean;
}

const List: React.FC<ListProps> = ({ data, limit, navigateTo, clickable, onRemove, isOrgManager }) => {
  const navigate = useNavigate();
  const [startIndex, setStartIndex] = useState(0); 

  const handlePrev = () => {
    if(startIndex == 0) {
      setStartIndex(data.length - 1); 
    }
    else {
      setStartIndex(startIndex - 1);
    }
  };

  const handleNext = () => {
    if(startIndex == data.length - 1) {
      setStartIndex(0); 
    }
    else {
      setStartIndex(startIndex + 1);
    }
  };

  const handleTitleOnClick = (postId: string | number) => {
    navigate(`/${navigateTo}/${postId}`);
  }

  const visibleItems1 = data.slice(startIndex, startIndex + limit);
  const visibleItems2 = data.slice(0, limit - visibleItems1.length);
  const visibleItems = visibleItems1.concat(visibleItems2);

  return (
    <div className="listComponent">
      <link
      href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
      rel="stylesheet"
      />

      {data.length > 1 && <button onClick={handlePrev} className="leftArrow"></button>}

      <div className="list">
        {visibleItems.map((item) => (
          <div className="itemAndButton">
          <div key={item.id} className={`listItem ${(clickable ? clickable(item.id) : false) ? "clickable" : "nonClickable"}`} onClick={() => handleTitleOnClick(item.id)}>
            <img
              src={item.image}
              alt={item.title}
              className="listItemImg"
            />
            <h3 className="listItemHeader">{item.title}</h3>
            <p className="listItemDesc">{item.description}</p>
          </div>
          {(isOrgManager && onRemove && item.image !== '/src/assets/defaultOrganizationDog.webp') && <button onClick={(e) => {e.stopPropagation(); onRemove(item.image);}} className="removeButton">X</button>}
          </div>
        ))}
      </div>

      {data.length > 1 && <button onClick={handleNext} className="rightArrow"></button>}


    </div>
  );
};

export default List;