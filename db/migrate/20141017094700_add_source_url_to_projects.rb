Sequel.migration do
  up do
    add_column :projects, :source_url, String
  end

  down do
    drop_column :projects, :source_url
  end
end
